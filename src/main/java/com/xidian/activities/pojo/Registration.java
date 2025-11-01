package com.xidian.activities.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 报名记录表 (registrations)
 */
@Data
public class Registration {
    /**
     * 报名ID，主键
     */
    private Long id;

    /**
     * 活动ID (关联activities.id)
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
     * 学生学院
     */
    private String studentCollege;

    /**
     * 状态：1-报名成功, 2-已取消
     */
    private Integer registrationStatus;

    /**
     * 签到状态：0-未签到, 1-已签到
     */
    private Integer checkInStatus;

    /**
     * 签到时间
     */
    private Date checkInTime;

    /**
     * 软删除标记
     */
    private Integer isDeleted;

    /**
     * 报名时间
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;
}
