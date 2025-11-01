package com.xidian.activities.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 活动表 (activities)
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
     * 活动类型编码 (关联activity_types.type_code)
     */
    private String activityType;

    /**
     * 状态：0-未发布, 1-报名中, 2-报名结束, 3-进行中, 4-已结束, 5-已取消
     */
    private Integer activityStatus;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

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
     * 最大报名人数 (NULL为不限)
     */
    private Integer maxParticipants;

    /**
     * 报名开始时间
     */
    private Date registrationStartTime;

    /**
     * 报名截止时间
     */
    private Date registrationEndTime;

    /**
     * 创建者ID (关联administrators.id)
     */
    private Long creatorId;

    /**
     * 软删除标记
     */
    private Integer isDeleted;

    /**
     * 记录创建时间
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;
}
