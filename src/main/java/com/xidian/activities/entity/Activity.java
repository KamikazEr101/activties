package com.xidian.activities.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体类 (activities)
 *
 * @author
 * @since
 */
@Data
public class Activity {

    /**
     * 活动ID，主键
     */
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String activityDescription;

    /**
     * 活动类型编码（关联activity_types.type_code）
     */
    private String activityType;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 主办方
     */
    private String organizer;

    /**
     * 负责人姓名
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 活动海报URL
     */
    private String posterUrl;

    /**
     * 最大参与人数（NULL为不限）
     */
    private Integer maxParticipants;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报名开始时间
     */
    private LocalDateTime registrationStartTime;

    /**
     * 报名截止时间
     */
    private LocalDateTime registrationEndTime;

    /**
     * 活动状态：0-未发布, 1-报名中, 2-报名结束, 3-进行中, 4-已结束, 5-已取消
     */
    private Integer activityStatus;

    /**
     * 创建者ID（管理员ID）
     */
    private Long creatorId;

    /**
     * 软删除标记：0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 活动类型信息（关联查询时使用，非数据库字段）
     */
    private ActivityType activityTypeInfo;
}