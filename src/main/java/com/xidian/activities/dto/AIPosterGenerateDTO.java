package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Data
@Schema(description = "AI海报生成请求")
public class AIPosterGenerateDTO {

    @Schema(description = "活动名称", requiredMode = RequiredMode.REQUIRED, example = "2024新生欢迎会")
    private String activityName;

    @Schema(description = "活动描述", example = "欢迎2024级新生加入我们的大家庭")
    private String activityDescription;

    @Schema(description = "活动地点", example = "体育馆")
    private String location;

    @Schema(description = "活动类型", example = "文艺活动")
    private String activityTypeName;

    @Schema(description = "开始时间", example = "2024-09-15 14:00")
    private String startTime;

    @Schema(description = "海报风格提示词（可选）", example = "现代简约风格，青春活力")
    private String stylePrompt;
}