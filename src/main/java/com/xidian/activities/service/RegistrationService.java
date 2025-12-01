package com.xidian.activities.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.dto.*;
import com.xidian.activities.vo.*;

/**
 * 报名服务接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public interface RegistrationService {

    /**
     * 学生报名活动
     *
     * @param registrationDTO 报名请求
     * @return 报名信息
     */
    RegistrationVO registerActivity(RegistrationDTO registrationDTO);

    /**
     * 取消报名
     *
     * @param registrationId 报名ID
     * @return 报名信息
     */
    RegistrationVO cancelRegistration(Long registrationId);

    /**
     * 学生签到
     *
     * @param checkInDTO 签到请求
     * @return 报名信息
     */
    RegistrationVO checkIn(CheckInDTO checkInDTO);

    /**
     * 通过Token签到(扫码签到)
     *
     * @param checkInByTokenDTO 扫码签到请求
     * @return 报名信息
     */
    RegistrationVO checkInByToken(CheckInByTokenDTO checkInByTokenDTO);

    /**
     * 生成活动签到二维码
     *
     * @param activityId 活动ID
     * @param baseUrl    网站基础URL(用于生成签到链接)
     * @return 签到二维码信息
     */
    CheckInQRCodeVO generateCheckInQRCode(Long activityId, String baseUrl);

    /**
     * 分页查询报名列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<RegistrationVO> getRegistrationList(RegistrationQueryDTO queryDTO);

    /**
     * 获取报名详情
     *
     * @param registrationId 报名ID
     * @return 报名信息
     */
    RegistrationVO getRegistrationDetail(Long registrationId);

    /**
     * 获取活动报名统计
     *
     * @param activityId 活动ID
     * @return 统计信息
     */
    RegistrationStatisticsVO getRegistrationStatistics(Long activityId);

    /**
     * 批量签到
     *
     * @param activityId   活动ID
     * @param phoneNumbers 手机号列表
     * @return 签到结果统计
     */
    BatchCheckInResult batchCheckIn(Long activityId, java.util.List<String> phoneNumbers);

    /**
     * 批量签到结果
     */
    class BatchCheckInResult {
        private int totalCount;
        private int successCount;
        private int failCount;
        private java.util.List<String> failedPhones;

        // getters and setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public java.util.List<String> getFailedPhones() {
            return failedPhones;
        }

        public void setFailedPhones(java.util.List<String> failedPhones) {
            this.failedPhones = failedPhones;
        }
    }

    /**
     * 根据手机号和姓名获取学生报名记录
     *
     * @param studentPhone 学生手机号
     * @param studentName  学生姓名
     * @return 报名记录列表
     */
    List<RegistrationVO> getStudentRegistrationRecordsByPhoneAndName(String studentPhone, String studentName);
}