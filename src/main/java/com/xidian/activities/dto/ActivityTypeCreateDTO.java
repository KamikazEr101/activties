package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * 创建活动类型DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "创建活动类型请求")
public class ActivityTypeCreateDTO {

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 50, message = "类型名称长度不能超过50个字符")
    @Schema(description = "类型名称", example = "学术讲座", requiredMode = RequiredMode.REQUIRED)
    private String typeName;

    @NotBlank(message = "类型编码不能为空")
    @Pattern(regexp = "^[A-Z_]{2,20}$", message = "类型编码只能包含大写字母和下划线，长度2-20位")
    @Schema(description = "类型编码", example = "ACADEMIC_LECTURE", requiredMode = RequiredMode.REQUIRED)
    private String typeCode;

    @NotNull(message = "排序顺序不能为空")
    @Min(value = 0, message = "排序顺序不能为负数")
    @Schema(description = "排序顺序，数字越小越靠前", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer sortOrder;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用，1为启用，0为禁用", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer isEnabled;
}