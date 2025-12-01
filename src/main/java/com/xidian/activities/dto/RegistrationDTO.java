package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 学生报名请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "学生报名请求")
public class RegistrationDTO {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", example = "1", required = true)
    private Long activityId;

    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 2, max = 50, message = "学生姓名长度必须在2-50字符之间")
    @Schema(description = "学生姓名", example = "张三", required = true)
    private String studentName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "学生手机号", example = "13800138000", required = true)
    private String studentPhone;

    @NotBlank(message = "学院不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100字符")
    @Schema(description = "学生学院", example = "计算机学院", required = true)
    private String studentCollege;

    @Schema(description = "备注信息", example = "希望参加此活动学习相关知识")
    private String remarks;
}