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
 * @author KamikazEr101
 * @since 2025/11/20
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
            // 惰性更新：如果当前时间已过报名截止时间，但状态仍为报名中，则更新状态
            if (ActivityStatusEnum.REGISTERING.getCode().equals(activity.getActivityStatus())) {
                log.info("触发惰性更新(报名): 活动[{}] 报名截止时间已过，更新状态为报名结束", activity.getId());
                activity.setActivityStatus(ActivityStatusEnum.REGISTRATION_ENDED.getCode());
                activity.setUpdateTime(now);
                activityMapper.updateById(activity);
                clearActivityCacheAfterRegistrationChange(activity.getId());
            }
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
        clearActivityCacheAfterRegistrationChange(activity.getId());

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
        clearActivityCacheAfterRegistrationChange(activity.getId());

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
            // 惰性更新：如果当前时间已过签到截止时间（活动结束+1小时），但状态仍未结束，则更新状态
            if (!ActivityStatusEnum.ENDED.getCode().equals(activity.getActivityStatus())
                    && !ActivityStatusEnum.CANCELLED.getCode().equals(activity.getActivityStatus())) {
                log.info("触发惰性更新(签到截止): 活动[{}] 签到截止时间已过，更新状态为已结束", activity.getId());
                activity.setActivityStatus(ActivityStatusEnum.ENDED.getCode());
                activity.setUpdateTime(now);
                activityMapper.updateById(activity);
                clearActivityCacheAfterRegistrationChange(activity.getId());
            }
            throw BizException.of(ResultCodeEnum.CHECKIN_ACTIVITY_ENDED);
        }

        // 更新签到状态
        registration.setCheckInStatus(1);
        registration.setCheckInTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 清理该活动的报名列表缓存
        clearActivityCacheAfterRegistrationChange(activity.getId());

        log.info("学生 {} 签到活动成功: {}", registration.getStudentPhone(), activity.getActivityName());

        return convertToVO(registration, activity);
    }

    @Override
    public PageInfo<RegistrationVO> getRegistrationList(RegistrationQueryDTO queryDTO) {
        // 直接查询数据库
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Registration> registrationList = registrationMapper.selectByConditions(queryDTO);

        List<RegistrationVO> voList = registrationList.stream()
                .map(registration -> {
                    Activity activity = activityMapper.selectById(registration.getActivityId());
                    return convertToVO(registration, activity);
                })
                .collect(Collectors.toList());

        return new PageInfo<>(voList);
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
        String qrContent = baseUrl + "/h5/checkin/validate?token=" + checkInToken;

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
            // 惰性更新：如果当前时间已过签到截止时间（活动结束+1小时），但状态仍未结束，则更新状态
            if (!ActivityStatusEnum.ENDED.getCode().equals(activity.getActivityStatus())
                    && !ActivityStatusEnum.CANCELLED.getCode().equals(activity.getActivityStatus())) {
                log.info("触发惰性更新(签到截止): 活动[{}] 签到截止时间已过，更新状态为已结束", activity.getId());
                activity.setActivityStatus(ActivityStatusEnum.ENDED.getCode());
                activity.setUpdateTime(now);
                activityMapper.updateById(activity);
                clearActivityCacheAfterRegistrationChange(activity.getId());
            }
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
     * 同时清理活动缓存，因为报名人数变化会影响活动详情和列表
     */
    private void clearActivityCacheAfterRegistrationChange(Long activityId) {
        try {
            // 1. 清理活动详情缓存（更新剩余名额）
            String activityDetailKey = CacheConstants.ACTIVITY_CACHE + activityId;
            redisService.delete(activityDetailKey);

            // 3. 清理活动列表缓存（更新报名进度）
            redisService.deleteByPrefix(CacheConstants.ACTIVITY_LIST_CACHE);

            log.info("清理缓存完成: activityId = {}", activityId);
        } catch (Exception e) {
            log.error("清理缓存失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
    }

    @Override
    public List<RegistrationVO> getStudentRegistrationRecordsByPhoneAndName(String studentPhone, String studentName) {
        return registrationMapper.selectByPhoneAndName(studentPhone, studentName);
    }
}