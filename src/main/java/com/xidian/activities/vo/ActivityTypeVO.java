package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动类型VO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动类型")
public class ActivityTypeVO {

    @Schema(description = "类型ID", example = "1")
    private Long id;

    @Schema(description = "类型编码", example = "ACADEMIC_LECTURE")
    private String typeCode;

    @Schema(description = "类型名称", example = "学术讲座")
    private String typeName;

    @Schema(description = "排序顺序，数字越小越靠前", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用，1为启用，0为禁用", example = "1")
    private Integer isEnabled;

    @Schema(description = "软删除标记：0-未删除, 1-已删除", example = "0")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}