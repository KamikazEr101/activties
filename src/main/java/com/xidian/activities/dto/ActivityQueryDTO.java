package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 活动查询请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "活动查询请求")
public class ActivityQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小必须大于0")
    @Schema(description = "页大小", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "活动名称关键词", example = "计算机")
    private String keyword;

    @Schema(description = "活动类型编码", example = "ACADEMIC")
    private String activityType;

    @Schema(description = "活动状态：0-未发布,1-报名中,2-报名结束,3-进行中,4-已结束,5-已取消", example = "1")
    private Integer activityStatus;

    @Schema(description = "创建者ID", example = "1")
    private Long creatorId;

    @Schema(description = "排序字段：createTime,startTime,registrationCount", example = "createTime")
    private String sortBy;

    @Schema(description = "排序方向：ASC,DESC", example = "DESC")
    private String sortOrder;
}