package com.xidian.activities.service;

import com.xidian.activities.dto.AIPosterGenerateDTO;
import com.xidian.activities.dto.AIImageResultDTO;

/**
 * AI服务接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public interface AIService {

    /**
     * 使用AI生成活动海报（仅生成图片，不上传MinIO）
     * 前端获取Base64图片后，可以选择调用 /file/upload/base64 接口上传
     *
     * @param generateDTO 活动信息
     * @return 图片Base64数据
     */
    AIImageResultDTO generateActivityPoster(AIPosterGenerateDTO generateDTO);
}
