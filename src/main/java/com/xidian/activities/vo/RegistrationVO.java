package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报名信息VO
 *
 * @author
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报名信息")
public class RegistrationVO {

    @Schema(description = "报名ID", example = "1")
    private Long id;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动名称", example = "新生计算机基础讲座")
    private String activityName;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生手机号", example = "13800138000")
    private String studentPhone;

    @Schema(description = "学生学院", example = "计算机学院")
    private String studentCollege;

    @Schema(description = "报名状态：1-报名成功, 2-已取消", example = "1")
    private Integer registrationStatus;

    @Schema(description = "报名状态名称", example = "报名成功")
    private String registrationStatusName;

    @Schema(description = "签到状态：0-未签到, 1-已签到", example = "0")
    private Integer checkInStatus;

    @Schema(description = "签到状态名称", example = "未签到")
    private String checkInStatusName;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "备注信息", example = "希望参加此活动学习相关知识")
    private String remarks;

    @Schema(description = "报名时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否可以取消报名", example = "true")
    private Boolean canCancel;

    @Schema(description = "是否可以签到", example = "true")
    private Boolean canCheckIn;
}