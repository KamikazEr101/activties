package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 报名查询请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "报名查询请求")
public class RegistrationQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小必须大于0")
    @Schema(description = "页大小", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "学生姓名关键词", example = "张")
    private String studentName;

    @Schema(description = "手机号", example = "13800138000")
    private String studentPhone;

    @Schema(description = "学院", example = "计算机学院")
    private String studentCollege;

    @Schema(description = "报名状态：1-报名成功, 2-已取消", example = "1")
    private Integer registrationStatus;

    @Schema(description = "签到状态：0-未签到, 1-已签到", example = "0")
    private Integer checkInStatus;

    @Schema(description = "排序字段：createTime,studentName", example = "createTime")
    private String sortBy;

    @Schema(description = "排序方向：ASC,DESC", example = "DESC")
    private String sortOrder;
}