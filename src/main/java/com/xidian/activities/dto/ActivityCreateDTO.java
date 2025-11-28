package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 活动创建请求DTO
 *
 * @author
 * @since
 */
@Data
@Schema(description = "活动创建请求")
public class ActivityCreateDTO {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称长度不能超过100字符")
    @Schema(description = "活动名称", example = "新生计算机基础讲座", required = true)
    private String activityName;

    @Size(max = 2000, message = "活动描述长度不能超过2000字符")
    @Schema(description = "活动描述", example = "由计算机学院主办，面向全校新生的计算机入门讲座")
    private String activityDescription;

    @NotBlank(message = "活动类型不能为空")
    @Schema(description = "活动类型编码", example = "ACADEMIC", required = true)
    private String activityType;

    @NotNull(message = "活动开始时间不能为空")
    @Schema(description = "活动开始时间", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    @Schema(description = "活动结束时间", required = true)
    private LocalDateTime endTime;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 200, message = "活动地点长度不能超过200字符")
    @Schema(description = "活动地点", example = "学术报告厅A201", required = true)
    private String location;

    @NotBlank(message = "主办方不能为空")
    @Size(max = 100, message = "主办方长度不能超过100字符")
    @Schema(description = "主办方", example = "计算机学院", required = true)
    private String organizer;

    @NotBlank(message = "负责人姓名不能为空")
    @Size(max = 50, message = "负责人姓名长度不能超过50字符")
    @Schema(description = "负责人姓名", example = "王老师", required = true)
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20字符")
    @Schema(description = "联系电话", example = "13812345678", required = true)
    private String contactPhone;

    @Min(value = 1, message = "最大参与人数必须大于0")
    @Schema(description = "最大参与人数，null表示不限制", example = "200")
    private Integer maxParticipants;

    @NotNull(message = "报名开始时间不能为空")
    @Schema(description = "报名开始时间", required = true)
    private LocalDateTime registrationStartTime;

    @NotNull(message = "报名截止时间不能为空")
    @Schema(description = "报名截止时间", required = true)
    private LocalDateTime registrationEndTime;

    @Schema(description = "活动海报URL", example = "https://example.com/poster.jpg")
    private String posterUrl;
}