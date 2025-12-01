package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 活动类型查询请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "活动类型查询请求")
public class ActivityTypeQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小必须大于0")
    @Schema(description = "页大小", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "类型名称关键词", example = "学术")
    private String typeName;

    @Schema(description = "类型编码", example = "ACADEMIC")
    private String typeCode;
}