package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 活动更新请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "活动更新请求")
public class ActivityUpdateDTO {

    @Schema(description = "活动ID", example = "1", required = true)
    private Long id;

    @Size(max = 100, message = "活动名称长度不能超过100字符")
    @Schema(description = "活动名称", example = "新生计算机基础讲座（更新版）")
    private String activityName;

    @Size(max = 2000, message = "活动描述长度不能超过2000字符")
    @Schema(description = "活动描述", example = "由计算机学院主办，面向全校新生的计算机入门讲座，新增了实践环节")
    private String activityDescription;

    @Schema(description = "活动类型编码", example = "ACADEMIC")
    private String activityType;

    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    @Schema(description = "报名结束时间")
    private LocalDateTime registrationEndTime;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Size(max = 200, message = "活动地点长度不能超过200字符")
    @Schema(description = "活动地点", example = "学术报告厅A201")
    private String location;

    @Size(max = 100, message = "主办方长度不能超过100字符")
    @Schema(description = "主办方", example = "计算机学院")
    private String organizer;

    @Size(max = 50, message = "负责人姓名长度不能超过50字符")
    @Schema(description = "负责人姓名", example = "王老师")
    private String contactPerson;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", example = "13812345678")
    private String contactPhone;

    @Min(value = 1, message = "最大参与人数必须大于0")
    @Schema(description = "最大参与人数，null表示不限制", example = "200")
    private Integer maxParticipants;

    @Schema(description = "活动海报URL", example = "https://example.com/poster.jpg")
    private String posterUrl;
}