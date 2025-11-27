package com.xidian.activities.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.enums.ActivityStatusEnum;
import com.xidian.activities.common.enums.RegistrationStatusEnum;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.*;
import com.xidian.activities.mapper.ActivityMapper;
import com.xidian.activities.mapper.RegistrationMapper;
import com.xidian.activities.entity.Activity;
import com.xidian.activities.entity.Registration;
import com.xidian.activities.service.RegistrationService;
import com.xidian.activities.service.RedisService;
import com.xidian.activities.constant.CacheConstants;
import com.xidian.activities.util.JwtUtil;
import com.xidian.activities.util.QRCodeUtil;
import com.xidian.activities.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报名服务实现类
 *
 * @author
 * @since
 */
@Slf4j
@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JwtUtil jwtUtil;

    // 报名状态映射
    private static final Map<Integer, String> REGISTRATION_STATUS_MAP = Map.of(
            1, "报名成功",
            2, "已取消");

    // 签到状态映射
    private static final Map<Integer, String> CHECK_IN_STATUS_MAP = Map.of(
            0, "未签到",
            1, "已签到");

    @Override
    @Transactional
    public RegistrationVO registerActivity(RegistrationDTO registrationDTO) {
        // 查询活动信息并加锁（防止超卖）
        // 注意：这里使用悲观锁，会阻塞其他事务对该活动的并发修改（如报名人数更新）
        Activity activity = activityMapper.selectByIdForUpdate(registrationDTO.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 验证活动状态
        if (!ActivityStatusEnum.REGISTERING.getCode().equals(activity.getActivityStatus())) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_STARTED);
        }

        // 验证报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getRegistrationStartTime())) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_STARTED);
        }
        if (now.isAfter(activity.getRegistrationEndTime())) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_ENDED);
        }

        // 验证人数限制
        if (activity.getMaxParticipants() != null) {
            int currentCount = registrationMapper.countValidRegistrations(activity.getId());
            if (currentCount >= activity.getMaxParticipants()) {
                throw BizException.of(ResultCodeEnum.ACTIVITY_FULL);
            }
        }

        // 检查重复报名（虽然数据库有唯一索引，但为了用户体验，先查一次）
        if (registrationMapper.existsByActivityAndPhone(activity.getId(), registrationDTO.getStudentPhone())) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_ALREADY_EXISTS);
        }

        // 创建报名记录
        Registration registration = new Registration();
        BeanUtils.copyProperties(registrationDTO, registration);
        registration.setRegistrationStatus(RegistrationStatusEnum.SUCCESS.getCode()); // 报名成功
        registration.setCheckInStatus(0); // 未签到
        registration.setIsDeleted(0);
        registration.setCreateTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());

        // 保存报名记录
        try {
            registrationMapper.insert(registration);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 捕获数据库唯一索引冲突异常
            throw BizException.of(ResultCodeEnum.REGISTRATION_ALREADY_EXISTS);
        }

        // 清理该活动的报名列表缓存
        clearRegistrationListCache(activity.getId());

        log.info("学生 {} 报名活动成功: {}", registration.getStudentPhone(), activity.getActivityName());

        return convertToVO(registration, activity);
    }

    @Override
    @Transactional
    public RegistrationVO cancelRegistration(Long registrationId) {
        // 查询报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || registration.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_FOUND);
        }

        // 检查是否可以取消（活动开始前可以取消）
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_CANCEL_TOO_LATE);
        }

        // 更新报名状态为已取消
        registration.setRegistrationStatus(2);
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 清理该活动的报名列表缓存
        clearRegistrationListCache(activity.getId());

        log.info("学生 {} 取消报名活动: {}", registration.getStudentPhone(), activity.getActivityName());

        return convertToVO(registration, activity);
    }

    @Override
    @Transactional
    public RegistrationVO checkIn(CheckInDTO checkInDTO) {
        // 查询报名记录
        Registration registration = registrationMapper.findByActivityAndPhone(
                checkInDTO.getActivityId(), checkInDTO.getStudentPhone());
        if (registration == null || registration.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_FOUND);
        }

        // 检查报名状态
        if (registration.getRegistrationStatus() != 1) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_ACTIVE);
        }

        // 检查是否已签到
        if (registration.getCheckInStatus() == 1) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_CHECKED_IN);
        }

        // 验证活动状态和时间
        Activity activity = activityMapper.selectById(checkInDTO.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 验证活动未被取消或删除
        if (activity.getActivityStatus() == 5) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_CANCELLED);
        }

        LocalDateTime now = LocalDateTime.now();
        // 签到时间：活动开始前30分钟至活动结束后1小时
        LocalDateTime checkInStartTime = activity.getStartTime().minusMinutes(30);
        LocalDateTime checkInEndTime = activity.getEndTime().plusHours(1);

        if (now.isBefore(checkInStartTime)) {
            throw BizException.of(ResultCodeEnum.CHECKIN_ACTIVITY_NOT_STARTED);
        }
        if (now.isAfter(checkInEndTime)) {
            throw BizException.of(ResultCodeEnum.CHECKIN_ACTIVITY_ENDED);
        }

        // 更新签到状态
        registration.setCheckInStatus(1);
        registration.setCheckInTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 清理该活动的报名列表缓存
        clearRegistrationListCache(activity.getId());

        log.info("学生 {} 签到活动成功: {}", registration.getStudentPhone(), activity.getActivityName());

        return convertToVO(registration, activity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageInfo<RegistrationVO> getRegistrationList(RegistrationQueryDTO queryDTO) {
        // 生成缓存key（基于查询条件）
        String cacheKey = generateRegistrationListCacheKey(queryDTO);

        // 先从缓存获取
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null && cached instanceof PageInfo) {
                log.info("从缓存获取报名列表: cacheKey = {}", cacheKey);
                return (PageInfo<RegistrationVO>) cached;
            }
        } catch (Exception e) {
            log.warn("从缓存获取报名列表失败: cacheKey = {}, error = {}", cacheKey, e.getMessage());
        }

        // 缓存未命中，查询数据库
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Registration> registrationList = registrationMapper.selectByConditions(queryDTO);

        List<RegistrationVO> voList = registrationList.stream()
                .map(registration -> {
                    Activity activity = activityMapper.selectById(registration.getActivityId());
                    return convertToVO(registration, activity);
                })
                .collect(Collectors.toList());

        PageInfo<RegistrationVO> pageInfo = new PageInfo<>(voList);

        // 写入缓存（5分钟过期，报名数据变化频繁）
        try {
            redisService.set(cacheKey, pageInfo, CacheConstants.SHORT_EXPIRE_TIME,
                    java.util.concurrent.TimeUnit.SECONDS);
            log.info("报名列表写入缓存: cacheKey = {}", cacheKey);
        } catch (Exception e) {
            log.error("报名列表写入缓存失败: cacheKey = {}, error = {}", cacheKey, e.getMessage());
        }

        return pageInfo;
    }

    @Override
    public RegistrationVO getRegistrationDetail(Long registrationId) {
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || registration.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.REGISTRATION_NOT_FOUND);
        }

        Activity activity = activityMapper.selectById(registration.getActivityId());
        return convertToVO(registration, activity);
    }

    @Override
    @Transactional
    public RegistrationVO checkInByToken(CheckInByTokenDTO checkInByTokenDTO) {
        // 验证签到Token
        if (!jwtUtil.validateCheckInToken(checkInByTokenDTO.getCheckInToken())) {
            throw BizException.of(ResultCodeEnum.TOKEN_INVALID, "签到二维码已失效，请刷新后重试");
        }

        // 从Token中获取活动ID
        Long activityId = jwtUtil.getActivityIdFromCheckInToken(checkInByTokenDTO.getCheckInToken());
        if (activityId == null) {
            throw BizException.of(ResultCodeEnum.TOKEN_INVALID, "无效的签到二维码");
        }

        // 构建CheckInDTO并调用原有签到逻辑
        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setActivityId(activityId);
        checkInDTO.setStudentPhone(checkInByTokenDTO.getStudentPhone());

        return checkIn(checkInDTO);
    }

    @Override
    public CheckInQRCodeVO generateCheckInQRCode(Long activityId, String baseUrl) {
        // 验证活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 生成签到Token(30分钟有效期)
        String checkInToken = jwtUtil.generateCheckInToken(activityId);

        // 生成签到链接
        String qrContent = baseUrl + "/h5/checkin?token=" + checkInToken;

        // 生成二维码图片
        byte[] qrCodeBytes;
        try {
            qrCodeBytes = QRCodeUtil.generateQRCode(qrContent);
        } catch (Exception e) {
            log.error("生成二维码失败: activityId={}, error={}", activityId, e.getMessage());
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR, "生成二维码失败");
        }

        // 转换为Base64
        String qrCodeImage = "data:image/png;base64," +
                java.util.Base64.getEncoder().encodeToString(qrCodeBytes);

        log.info("生成活动签到二维码成功: activityId={}, activityName={}", activityId, activity.getActivityName());

        CheckInQRCodeVO vo = new CheckInQRCodeVO();
        vo.setQrContent(qrContent);
        vo.setCheckInToken(checkInToken);
        vo.setQrCodeImage(qrCodeImage);
        vo.setExpiresIn(30 * 60L); // 30分钟

        return vo;
    }

    @Override
    public RegistrationStatisticsVO getRegistrationStatistics(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 获取统计数据
        int totalRegistrations = registrationMapper.countTotalRegistrations(activityId);
        int validRegistrations = registrationMapper.countValidRegistrations(activityId);
        int cancelledRegistrations = totalRegistrations - validRegistrations;
        int checkedInCount = registrationMapper.countCheckedIn(activityId);
        int notCheckedInCount = validRegistrations - checkedInCount;

        // 构建统计VO
        return RegistrationStatisticsVO.builder()
                .activityId(activityId)
                .activityName(activity.getActivityName())
                .maxParticipants(activity.getMaxParticipants())
                .totalRegistrations(totalRegistrations)
                .validRegistrations(validRegistrations)
                .cancelledRegistrations(cancelledRegistrations)
                .checkedInCount(checkedInCount)
                .notCheckedInCount(notCheckedInCount)
                .remainingSlots(
                        activity.getMaxParticipants() != null ? activity.getMaxParticipants() - validRegistrations
                                : null)
                .registrationProgress(activity.getMaxParticipants() != null
                        ? (double) validRegistrations / activity.getMaxParticipants() * 100
                        : null)
                .checkInRate(validRegistrations > 0 ? (double) checkedInCount / validRegistrations * 100 : 0.0)
                .attendanceRate(validRegistrations > 0 ? (double) checkedInCount / validRegistrations * 100 : 0.0)
                .build();
    }

    @Override
    @Transactional
    public BatchCheckInResult batchCheckIn(Long activityId, List<String> phoneNumbers) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND);
        }

        // 验证活动未被取消
        if (activity.getActivityStatus() == 5) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_CANCELLED);
        }

        BatchCheckInResult result = new BatchCheckInResult();
        result.setTotalCount(phoneNumbers.size());
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailedPhones(new java.util.ArrayList<>());

        // 验证签到时间范围：活动开始前30分钟至结束后1小时
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInStartTime = activity.getStartTime().minusMinutes(30);
        LocalDateTime checkInEndTime = activity.getEndTime().plusHours(1);

        if (now.isBefore(checkInStartTime)) {
            throw BizException.of(ResultCodeEnum.CHECKIN_ACTIVITY_NOT_STARTED);
        }
        if (now.isAfter(checkInEndTime)) {
            throw BizException.of(ResultCodeEnum.CHECKIN_ACTIVITY_ENDED);
        }

        for (String phone : phoneNumbers) {
            try {
                CheckInDTO checkInDTO = new CheckInDTO();
                checkInDTO.setActivityId(activityId);
                checkInDTO.setStudentPhone(phone);

                checkIn(checkInDTO);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailedPhones().add(phone);
                log.warn("批量签到失败: 手机号 {}, 错误: {}", phone, e.getMessage());
            }
        }

        log.info("批量签到完成: 活动ID {}, 总数 {}, 成功 {}, 失败 {}",
                activityId, result.getTotalCount(), result.getSuccessCount(), result.getFailCount());

        return result;
    }

    /**
     * 转换为VO
     */
    private RegistrationVO convertToVO(Registration registration, Activity activity) {
        RegistrationVO vo = new RegistrationVO();
        BeanUtils.copyProperties(registration, vo);

        vo.setRegistrationStatusName(
                REGISTRATION_STATUS_MAP.getOrDefault(registration.getRegistrationStatus(), "未知状态"));
        vo.setCheckInStatusName(CHECK_IN_STATUS_MAP.getOrDefault(registration.getCheckInStatus(), "未知状态"));

        if (activity != null) {
            vo.setActivityName(activity.getActivityName());

            // 设置是否可以取消报名
            LocalDateTime now = LocalDateTime.now();
            boolean canCancel = registration.getRegistrationStatus() == 1
                    && now.isBefore(activity.getStartTime());
            vo.setCanCancel(canCancel);

            // 设置是否可以签到（活动开始前30分钟至活动结束后1小时）
            LocalDateTime checkInStartTime = activity.getStartTime().minusMinutes(30);
            LocalDateTime checkInEndTime = activity.getEndTime().plusHours(1);
            boolean canCheckIn = registration.getRegistrationStatus() == 1
                    && registration.getCheckInStatus() == 0
                    && activity.getActivityStatus() != 5 // 活动未被取消
                    && now.isAfter(checkInStartTime)
                    && now.isBefore(checkInEndTime);
            vo.setCanCheckIn(canCheckIn);
        }

        return vo;
    }

    /**
     * 清理报名列表缓存
     */
    private void clearRegistrationListCache(Long activityId) {
        try {
            // 清理该活动的所有报名列表缓存（使用前缀匹配删除）
            String cachePrefix = CacheConstants.REGISTRATION_LIST_CACHE + activityId;
            Long deletedCount = redisService.deleteByPrefix(cachePrefix);
            log.info("清理报名列表缓存: activityId = {}, 清理缓存数量 = {}", activityId, deletedCount);
        } catch (Exception e) {
            log.error("清理报名列表缓存失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
    }

    /**
     * 生成报名列表缓存key
     */
    private String generateRegistrationListCacheKey(RegistrationQueryDTO queryDTO) {
        // 基于查询条件生成缓存key
        StringBuilder keyBuilder = new StringBuilder(CacheConstants.REGISTRATION_LIST_CACHE);

        // 活动ID是必须的，作为第一级key
        if (queryDTO.getActivityId() != null) {
            keyBuilder.append(queryDTO.getActivityId());
        } else {
            keyBuilder.append("all");
        }

        // 分页参数
        keyBuilder.append(":page:").append(queryDTO.getPageNum())
                .append(":size:").append(queryDTO.getPageSize());

        // 筛选条件
        if (queryDTO.getStudentName() != null && !queryDTO.getStudentName().isEmpty()) {
            keyBuilder.append(":name:").append(queryDTO.getStudentName());
        }
        if (queryDTO.getStudentPhone() != null && !queryDTO.getStudentPhone().isEmpty()) {
            keyBuilder.append(":phone:").append(queryDTO.getStudentPhone());
        }
        if (queryDTO.getStudentCollege() != null && !queryDTO.getStudentCollege().isEmpty()) {
            keyBuilder.append(":college:").append(queryDTO.getStudentCollege());
        }
        if (queryDTO.getRegistrationStatus() != null) {
            keyBuilder.append(":regStatus:").append(queryDTO.getRegistrationStatus());
        }
        if (queryDTO.getCheckInStatus() != null) {
            keyBuilder.append(":checkStatus:").append(queryDTO.getCheckInStatus());
        }
        if (queryDTO.getSortBy() != null) {
            keyBuilder.append(":sort:").append(queryDTO.getSortBy())
                    .append(":").append(queryDTO.getSortOrder() != null ? queryDTO.getSortOrder() : "DESC");
        }

        return keyBuilder.toString();
    }
}