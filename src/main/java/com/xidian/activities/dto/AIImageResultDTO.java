package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成图片结果
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI生成图片结果")
public class AIImageResultDTO {

    @Schema(description = "图片Base64数据", example = "data:image/png;base64,iVBORw0KGg...")
    private String imageBase64;

    @Schema(description = "图片格式", example = "png")
    private String format;

    @Schema(description = "生成提示词", example = "2024新生欢迎会海报...")
    private String prompt;
}
