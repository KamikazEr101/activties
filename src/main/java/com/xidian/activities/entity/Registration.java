package com.xidian.activities.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名记录实体类 (registrations)
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
public class Registration {

    /**
     * 报名记录ID，主键
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生手机号
     */
    private String studentPhone;

    /**
     * 学院
     */
    private String studentCollege;

    /**
     * 报名状态：1-报名成功, 2-已取消
     */
    private Integer registrationStatus;

    /**
     * 签到状态：0-未签到, 1-已签到
     */
    private Integer checkInStatus;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 软删除标记：0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间（报名时间）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 活动信息（关联查询时使用，非数据库字段）
     */
    private Activity activity;
}