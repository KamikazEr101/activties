package com.xidian.activities.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.enums.ActivityStatusEnum;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.login.LoginUserHolder;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.*;
import com.xidian.activities.mapper.ActivityMapper;
import com.xidian.activities.mapper.ActivityTypeMapper;
import com.xidian.activities.mapper.AdministratorMapper;
import com.xidian.activities.entity.Activity;
import com.xidian.activities.entity.ActivityType;
import com.xidian.activities.entity.Administrator;
import com.xidian.activities.service.ActivityService;
import com.xidian.activities.service.RedisService;
import com.xidian.activities.constant.CacheConstants;
import com.xidian.activities.vo.ActivityDetailVO;
import com.xidian.activities.vo.ActivityListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 *
 * @author
 * @since
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityTypeMapper activityTypeMapper;

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private RedisService redisService;

    // 活动状态映射
    private static final Map<Integer, String> ACTIVITY_STATUS_MAP = Arrays.stream(ActivityStatusEnum.values())
            .collect(Collectors.toMap(ActivityStatusEnum::getCode, ActivityStatusEnum::getDescription));

    @Override
    @Transactional
    public ActivityDetailVO createActivity(ActivityCreateDTO createDTO) {
        // 获取当前管理员ID
        Long adminId = LoginUserHolder.getAdminId();
        if (adminId == null) {
            throw BizException.of(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        // 验证活动类型
        ActivityType activityType = activityTypeMapper.selectByTypeCode(createDTO.getActivityType());
        if (activityType == null || activityType.getIsEnabled() == 0) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_TYPE_INVALID);
        }

        // 验证时间逻辑
        validateActivityTime(createDTO);

        // 创建活动对象
        Activity activity = new Activity();
        BeanUtils.copyProperties(createDTO, activity);
        activity.setCreatorId(adminId);
        activity.setActivityStatus(ActivityStatusEnum.UNRELEASED.getCode()); // 默认未发布
        activity.setIsDeleted(0);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        // 保存活动
        activityMapper.insert(activity);

        // 清理缓存（新增活动后，列表缓存需要更新）
        clearActivityCache(activity.getId());

        log.info("管理员 {} 创建活动成功: {}", adminId, activity.getActivityName());

        return convertToDetailVO(activity);
    }

    @Override
    @Transactional
    public ActivityDetailVO updateActivity(ActivityUpdateDTO updateDTO) {
        // 获取当前管理员ID
        Long adminId = LoginUserHolder.getAdminId();
        if (adminId == null) {
            throw BizException.of(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        // 查询原活动信息
        Activity activity = activityMapper.selectById(updateDTO.getId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 检查权限：只有创建者或超级管理员可以修改
        com.xidian.activities.common.login.LoginUser currentUser = LoginUserHolder.getLoginUser();
        if (!activity.getCreatorId().equals(adminId) && currentUser.getRoleType() != 2) {
            throw BizException.of(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        }

        // 检查时间限制：报名开始后不能修改任何信息
        // 特例：如果活动还未发布，即使时间过了也可以修改（为了修正错误的时间）
        LocalDateTime now = LocalDateTime.now();
        if (activity.getActivityStatus() != ActivityStatusEnum.UNRELEASED.getCode()
                && activity.getRegistrationStartTime() != null
                && now.isAfter(activity.getRegistrationStartTime())) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_REGISTRATION_STARTED);
        }

        // 更新活动信息
        BeanUtils.copyProperties(updateDTO, activity, "id", "creatorId", "createTime", "isDeleted");

        // 如果活动已发布，需要根据新的时间重新计算状态
        if (activity.getActivityStatus() != ActivityStatusEnum.UNRELEASED.getCode()
                && activity.getActivityStatus() != ActivityStatusEnum.CANCELLED.getCode()) {
            recalculateActivityStatus(activity);
        }

        activity.setUpdateTime(LocalDateTime.now());

        activityMapper.updateById(activity);

        // 清理缓存
        clearActivityCache(activity.getId());

        log.info("管理员 {} 更新活动成功: {}", adminId, activity.getActivityName());

        return convertToDetailVO(activity);
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        Long adminId = LoginUserHolder.getAdminId();
        if (adminId == null) {
            throw BizException.of(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        com.xidian.activities.common.login.LoginUser currentUser = LoginUserHolder.getLoginUser();
        if (!activity.getCreatorId().equals(adminId) && currentUser.getRoleType() != 2) {
            throw BizException.of(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        }

        // 允许删除的状态：未发布(0)、已结束(4)、已取消(5)
        // 不允许删除的状态：报名中(1)、报名结束(2)、进行中(3)
        if (activity.getActivityStatus() == ActivityStatusEnum.REGISTERING.getCode()
                || activity.getActivityStatus() == ActivityStatusEnum.REGISTRATION_ENDED.getCode()
                || activity.getActivityStatus() == ActivityStatusEnum.IN_PROGRESS.getCode()) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_DELETABLE);
        }

        activity.setIsDeleted(1);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);

        // 清理缓存
        clearActivityCache(activity.getId());

        log.info("管理员 {} 删除活动成功: {}", adminId, activity.getActivityName());
    }

    @Override
    @Transactional
    public ActivityDetailVO updateActivityStatus(ActivityStatusUpdateDTO statusUpdateDTO) {
        Long adminId = LoginUserHolder.getAdminId();
        if (adminId == null) {
            throw BizException.of(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        Activity activity = activityMapper.selectById(statusUpdateDTO.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        com.xidian.activities.common.login.LoginUser currentUser = LoginUserHolder.getLoginUser();
        if (!activity.getCreatorId().equals(adminId) && currentUser.getRoleType() != 2) {
            throw BizException.of(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        }

        validateStatusTransition(activity.getActivityStatus(), statusUpdateDTO.getActivityStatus());

        activity.setActivityStatus(statusUpdateDTO.getActivityStatus());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);

        // 清理缓存
        clearActivityCache(activity.getId());

        log.info("管理员 {} 更新活动 {} 状态: {} -> {}",
                adminId, activity.getActivityName(),
                activity.getActivityStatus(), statusUpdateDTO.getActivityStatus());

        return convertToDetailVO(activity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageInfo<ActivityListVO> getActivityList(ActivityQueryDTO queryDTO) {
        // 生成缓存key（基于查询条件）
        String cacheKey = generateActivityListCacheKey(queryDTO);

        // 先从缓存获取
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null && cached instanceof PageInfo) {
                log.info("从缓存获取活动列表: cacheKey = {}", cacheKey);
                return (PageInfo<ActivityListVO>) cached;
            }
        } catch (Exception e) {
            log.warn("从缓存获取活动列表失败: cacheKey = {}, error = {}", cacheKey, e.getMessage());
        }

        // 缓存未命中，查询数据库
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Activity> activityList = activityMapper.selectList(queryDTO);

        List<ActivityListVO> voList = activityList.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageInfo<ActivityListVO> pageInfo = new PageInfo<>(voList);

        // 写入缓存（10分钟过期，列表数据变化较频繁）
        try {
            redisService.set(cacheKey, pageInfo, 600L, java.util.concurrent.TimeUnit.SECONDS);
            log.info("活动列表写入缓存: cacheKey = {}", cacheKey);
        } catch (Exception e) {
            log.error("活动列表写入缓存失败: cacheKey = {}, error = {}", cacheKey, e.getMessage());
        }

        return pageInfo;
    }

    @Override
    public ActivityDetailVO getActivityDetail(Long activityId) {
        // 先从缓存获取
        String cacheKey = CacheConstants.ACTIVITY_CACHE + activityId;
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null && cached instanceof ActivityDetailVO) {
                log.info("从缓存获取活动详情: activityId = {}", activityId);
                return (ActivityDetailVO) cached;
            }
        } catch (Exception e) {
            log.warn("从缓存获取活动详情失败: activityId = {}, error = {}", activityId, e.getMessage());
        }

        // 缓存未命中，查询数据库
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 注意：此方法不检查活动状态，状态检查由调用方（Controller层）负责
        // - ActivityController（管理员端）：不检查状态，管理员可查看所有状态的活动
        // - PublicController（学生端）：检查状态，学生只能查看已发布的活动
        ActivityDetailVO detailVO = convertToDetailVO(activity);

        // 写入缓存（30分钟过期）
        try {
            redisService.set(cacheKey, detailVO, CacheConstants.DEFAULT_EXPIRE_TIME,
                    java.util.concurrent.TimeUnit.SECONDS);
            log.info("活动详情写入缓存: activityId = {}", activityId);
        } catch (Exception e) {
            log.error("活动详情写入缓存失败: activityId = {}, error = {}", activityId, e.getMessage());
        }

        return detailVO;
    }

    @Override
    @Transactional
    public ActivityDetailVO publishActivity(Long activityId) {
        // 查询活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 活动发布前的完整性检查
        validateActivityCompleteness(activity);

        ActivityStatusUpdateDTO statusUpdateDTO = new ActivityStatusUpdateDTO();
        statusUpdateDTO.setActivityId(activityId);
        statusUpdateDTO.setActivityStatus(ActivityStatusEnum.REGISTERING.getCode()); // 报名中

        return updateActivityStatus(statusUpdateDTO);
    }

    @Override
    @Transactional
    public ActivityDetailVO cancelActivity(Long activityId) {
        ActivityStatusUpdateDTO statusUpdateDTO = new ActivityStatusUpdateDTO();
        statusUpdateDTO.setActivityId(activityId);
        statusUpdateDTO.setActivityStatus(ActivityStatusEnum.CANCELLED.getCode()); // 已取消

        return updateActivityStatus(statusUpdateDTO);
    }

    private void validateActivityTime(ActivityCreateDTO createDTO) {
        // 活动结束时间必须晚于开始时间
        if (createDTO.getStartTime().isAfter(createDTO.getEndTime())) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "活动结束时间必须晚于开始时间");
        }

        // 报名截止时间必须晚于报名开始时间
        if (createDTO.getRegistrationStartTime().isAfter(createDTO.getRegistrationEndTime())) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "报名截止时间必须晚于报名开始时间");
        }

        // 报名截止时间不能晚于活动开始时间
        if (createDTO.getRegistrationEndTime().isAfter(createDTO.getStartTime())) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "报名截止时间不能晚于活动开始时间");
        }
    }

    /**
     * 验证活动信息完整性（发布前检查）
     */
    private void validateActivityCompleteness(Activity activity) {
        StringBuilder errorMsg = new StringBuilder();

        // 验证活动名称
        if (activity.getActivityName() == null || activity.getActivityName().trim().isEmpty()) {
            errorMsg.append("活动名称不能为空; ");
        }

        // 验证活动描述
        if (activity.getActivityDescription() == null || activity.getActivityDescription().trim().isEmpty()) {
            errorMsg.append("活动描述不能为空; ");
        }

        // 验证活动海报
        if (activity.getPosterUrl() == null || activity.getPosterUrl().trim().isEmpty()) {
            errorMsg.append("活动海报不能为空; ");
        }

        // 验证活动地点
        if (activity.getLocation() == null || activity.getLocation().trim().isEmpty()) {
            errorMsg.append("活动地点不能为空; ");
        }

        // 验证负责人信息
        if (activity.getContactPerson() == null || activity.getContactPerson().trim().isEmpty()) {
            errorMsg.append("负责人姓名不能为空; ");
        }

        // 验证联系方式
        if (activity.getContactPhone() == null || activity.getContactPhone().trim().isEmpty()) {
            errorMsg.append("联系电话不能为空; ");
        }

        // 验证时间设置
        if (activity.getStartTime() == null) {
            errorMsg.append("活动开始时间不能为空; ");
        }
        if (activity.getEndTime() == null) {
            errorMsg.append("活动结束时间不能为空; ");
        }
        if (activity.getRegistrationStartTime() == null) {
            errorMsg.append("报名开始时间不能为空; ");
        }
        if (activity.getRegistrationEndTime() == null) {
            errorMsg.append("报名截止时间不能为空; ");
        }

        // 验证报名名额（如果设置了上限）
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() <= 0) {
            errorMsg.append("报名名额必须大于0; ");
        }

        // 如果有错误信息，抛出异常
        if (errorMsg.length() > 0) {
            // 移除最后的分号和空格
            String finalMsg = errorMsg.substring(0, errorMsg.length() - 2);
            throw BizException.of(ResultCodeEnum.ACTIVITY_INFO_INCOMPLETE, finalMsg);
        }

        // 验证时间的合理性（如果所有时间都不为空）
        if (activity.getStartTime() != null && activity.getEndTime() != null
                && activity.getRegistrationStartTime() != null && activity.getRegistrationEndTime() != null) {

            if (activity.getStartTime().isAfter(activity.getEndTime())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "活动结束时间必须晚于开始时间");
            }

            if (activity.getRegistrationStartTime().isAfter(activity.getRegistrationEndTime())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "报名截止时间必须晚于报名开始时间");
            }

            if (activity.getRegistrationEndTime().isAfter(activity.getStartTime())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_TIME_INVALID, "报名截止时间不能晚于活动开始时间");
            }
        }
    }

    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus.equals(ActivityStatusEnum.UNRELEASED.getCode())) {
            if (!newStatus.equals(ActivityStatusEnum.REGISTERING.getCode())
                    && !newStatus.equals(ActivityStatusEnum.CANCELLED.getCode())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "未发布的活动只能转为报名中或已取消状态");
            }
        } else if (currentStatus.equals(ActivityStatusEnum.REGISTERING.getCode())) {
            if (!newStatus.equals(ActivityStatusEnum.REGISTRATION_ENDED.getCode())
                    && !newStatus.equals(ActivityStatusEnum.CANCELLED.getCode())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "报名中的活动只能转为报名结束或已取消状态");
            }
        } else if (currentStatus.equals(ActivityStatusEnum.REGISTRATION_ENDED.getCode())) {
            if (!newStatus.equals(ActivityStatusEnum.IN_PROGRESS.getCode())
                    && !newStatus.equals(ActivityStatusEnum.CANCELLED.getCode())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "报名结束的活动只能转为进行中或已取消状态");
            }
        } else if (currentStatus.equals(ActivityStatusEnum.IN_PROGRESS.getCode())) {
            if (!newStatus.equals(ActivityStatusEnum.ENDED.getCode())
                    && !newStatus.equals(ActivityStatusEnum.CANCELLED.getCode())) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "进行中的活动只能转为已结束或已取消状态");
            }
        } else if (currentStatus.equals(ActivityStatusEnum.ENDED.getCode())
                || currentStatus.equals(ActivityStatusEnum.CANCELLED.getCode())) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "已结束或已取消的活动无法修改状态");
        } else {
            throw BizException.of(ResultCodeEnum.ACTIVITY_STATUS_INVALID, "无效的活动状态");
        }
    }

    /**
     * 根据当前时间和活动时间重新计算活动状态
     */
    private void recalculateActivityStatus(Activity activity) {
        LocalDateTime now = LocalDateTime.now();

        // 如果活动已取消或未发布，不重新计算
        if (activity.getActivityStatus() == ActivityStatusEnum.UNRELEASED.getCode()
                || activity.getActivityStatus() == ActivityStatusEnum.CANCELLED.getCode()) {
            return;
        }

        int newStatus;
        if (now.isAfter(activity.getEndTime())) {
            newStatus = ActivityStatusEnum.ENDED.getCode(); // 已结束
        } else if (now.isAfter(activity.getStartTime())) {
            newStatus = ActivityStatusEnum.IN_PROGRESS.getCode(); // 进行中
        } else if (now.isAfter(activity.getRegistrationEndTime())) {
            newStatus = ActivityStatusEnum.REGISTRATION_ENDED.getCode(); // 报名结束
        } else {
            newStatus = ActivityStatusEnum.REGISTERING.getCode(); // 报名中
        }

        // 只有状态发生变化时才更新
        if (activity.getActivityStatus() != newStatus) {
            log.info("活动[{}]状态因时间变更自动调整: {} -> {}",
                    activity.getId(), activity.getActivityStatus(), newStatus);
            activity.setActivityStatus(newStatus);
        }
    }

    private ActivityDetailVO convertToDetailVO(Activity activity) {
        ActivityDetailVO vo = new ActivityDetailVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setActivityStatusName(ACTIVITY_STATUS_MAP.getOrDefault(activity.getActivityStatus(), "未知状态"));

        ActivityType activityType = activityTypeMapper.selectByTypeCode(activity.getActivityType());
        if (activityType != null) {
            vo.setActivityTypeName(activityType.getTypeName());
        }

        Administrator creator = administratorMapper.selectById(activity.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getRealName());
        }

        int currentCount = activityMapper.countRegistrations(activity.getId());
        vo.setCurrentRegistrationCount(currentCount);

        if (activity.getMaxParticipants() != null) {
            vo.setRemainingSlots(activity.getMaxParticipants() - currentCount);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean canRegister = activity.getActivityStatus() == 1
                && now.isAfter(activity.getRegistrationStartTime())
                && now.isBefore(activity.getRegistrationEndTime());
        vo.setCanRegister(canRegister);

        boolean canCancel = activity.getActivityStatus() < ActivityStatusEnum.REGISTRATION_ENDED.getCode();
        vo.setCanCancel(canCancel);

        // 获取访问次数（从 Redis 获取）
        Long viewCount = getViewCount(activity.getId());
        vo.setViewCount(viewCount);

        return vo;
    }

    private ActivityListVO convertToListVO(Activity activity) {
        ActivityListVO vo = new ActivityListVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setActivityStatusName(ACTIVITY_STATUS_MAP.getOrDefault(activity.getActivityStatus(), "未知状态"));

        ActivityType activityType = activityTypeMapper.selectByTypeCode(activity.getActivityType());
        if (activityType != null) {
            vo.setActivityTypeName(activityType.getTypeName());
        }

        Administrator creator = administratorMapper.selectById(activity.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getRealName());
        }

        int currentCount = activityMapper.countRegistrations(activity.getId());
        vo.setCurrentRegistrationCount(currentCount);

        if (activity.getMaxParticipants() != null) {
            vo.setRemainingSlots(activity.getMaxParticipants() - currentCount);
            vo.setRegistrationProgress((double) currentCount / activity.getMaxParticipants() * 100);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean canRegister = ActivityStatusEnum.REGISTERING.getCode().equals(activity.getActivityStatus())
                && now.isAfter(activity.getRegistrationStartTime())
                && now.isBefore(activity.getRegistrationEndTime());
        vo.setCanRegister(canRegister);

        return vo;
    }

    /**
     * 清理活动相关缓存
     */
    private void clearActivityCache(Long activityId) {
        try {
            // 清理活动详情缓存
            String detailCacheKey = CacheConstants.ACTIVITY_CACHE + activityId;
            redisService.delete(detailCacheKey);

            // 清理活动列表缓存（使用前缀匹配删除所有列表缓存）
            Long deletedCount = redisService.deleteByPrefix(CacheConstants.ACTIVITY_LIST_CACHE);
            log.info("清理活动缓存: activityId = {}, 清理列表缓存数量 = {}", activityId, deletedCount);
        } catch (Exception e) {
            log.error("清理活动缓存失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
    }

    /**
     * 生成活动列表缓存key
     */
    private String generateActivityListCacheKey(ActivityQueryDTO queryDTO) {
        // 基于查询条件生成缓存key
        StringBuilder keyBuilder = new StringBuilder(CacheConstants.ACTIVITY_LIST_CACHE);
        keyBuilder.append("page:").append(queryDTO.getPageNum())
                .append(":size:").append(queryDTO.getPageSize());

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            keyBuilder.append(":kw:").append(queryDTO.getKeyword());
        }
        if (queryDTO.getActivityType() != null) {
            keyBuilder.append(":type:").append(queryDTO.getActivityType());
        }
        if (queryDTO.getActivityStatus() != null) {
            keyBuilder.append(":status:").append(queryDTO.getActivityStatus());
        }
        if (queryDTO.getCreatorId() != null) {
            keyBuilder.append(":creator:").append(queryDTO.getCreatorId());
        }
        if (queryDTO.getSortBy() != null) {
            keyBuilder.append(":sort:").append(queryDTO.getSortBy())
                    .append(":").append(queryDTO.getSortOrder() != null ? queryDTO.getSortOrder() : "DESC");
        }

        return keyBuilder.toString();
    }

    @Override
    public void incrementViewCount(Long activityId) {
        try {
            String viewCountKey = CacheConstants.ACTIVITY_VIEW_COUNT_CACHE + activityId;
            // 使用 Redis 的 increment 操作，原子性增加计数
            redisService.increment(viewCountKey, 1L);
            log.debug("活动访问次数+1: activityId = {}", activityId);
        } catch (Exception e) {
            // 访问统计失败不影响主流程
            log.warn("记录活动访问次数失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
    }

    /**
     * 获取活动访问次数
     */
    private Long getViewCount(Long activityId) {
        try {
            String viewCountKey = CacheConstants.ACTIVITY_VIEW_COUNT_CACHE + activityId;
            Object count = redisService.get(viewCountKey);
            if (count != null) {
                if (count instanceof Long) {
                    return (Long) count;
                } else if (count instanceof Integer) {
                    return ((Integer) count).longValue();
                } else if (count instanceof String) {
                    return Long.parseLong((String) count);
                }
            }
        } catch (Exception e) {
            log.warn("获取活动访问次数失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
        return 0L; // 默认返回 0
    }
}