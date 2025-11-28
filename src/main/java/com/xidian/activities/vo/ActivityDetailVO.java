package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动详情VO
 *
 * @author
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动详情")
public class ActivityDetailVO {

    @Schema(description = "活动ID", example = "1")
    private Long id;

    @Schema(description = "活动名称", example = "新生计算机基础讲座")
    private String activityName;

    @Schema(description = "活动描述", example = "由计算机学院主办，面向全校新生的计算机入门讲座")
    private String activityDescription;

    @Schema(description = "活动类型编码", example = "ACADEMIC")
    private String activityType;

    @Schema(description = "活动类型名称", example = "学术讲座")
    private String activityTypeName;

    @Schema(description = "活动状态：0-未发布,1-报名中,2-报名结束,3-进行中,4-已结束,5-已取消", example = "1")
    private Integer activityStatus;

    @Schema(description = "活动状态名称", example = "报名中")
    private String activityStatusName;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动地点", example = "学术报告厅A201")
    private String location;

    @Schema(description = "主办方", example = "计算机学院")
    private String organizer;

    @Schema(description = "负责人姓名", example = "王老师")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13812345678")
    private String contactPhone;

    @Schema(description = "活动海报URL", example = "https://example.com/poster.jpg")
    private String posterUrl;

    @Schema(description = "最大参与人数，null表示不限制", example = "200")
    private Integer maxParticipants;

    @Schema(description = "当前报名人数", example = "150")
    private Integer currentRegistrationCount;

    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    @Schema(description = "报名结束时间")
    private LocalDateTime registrationEndTime;

    @Schema(description = "创建者ID", example = "1")
    private Long creatorId;

    @Schema(description = "创建者姓名", example = "管理员")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否可以报名", example = "true")
    private Boolean canRegister;

    @Schema(description = "是否可以取消", example = "true")
    private Boolean canCancel;

    @Schema(description = "剩余名额，null表示不限制", example = "50")
    private Integer remainingSlots;

    @Schema(description = "访问次数", example = "1250")
    private Long viewCount;
}