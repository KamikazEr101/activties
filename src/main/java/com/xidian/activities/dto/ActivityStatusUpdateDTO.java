package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 活动状态更新请求DTO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Schema(description = "活动状态更新请求")
public class ActivityStatusUpdateDTO {

    @Schema(description = "活动ID", example = "1", required = true)
    private Long activityId;

    @NotNull(message = "活动状态不能为空")
    @Min(value = 0, message = "活动状态值不正确")
    @Max(value = 5, message = "活动状态值不正确")
    @Schema(description = "活动状态：0-未发布,1-报名中,2-报名结束,3-进行中,4-已结束,5-已取消", example = "1", required = true)
    private Integer activityStatus;
}