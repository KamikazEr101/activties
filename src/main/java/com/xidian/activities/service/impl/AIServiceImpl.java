package com.xidian.activities.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.AIPosterGenerateDTO;
import com.xidian.activities.dto.AIImageResultDTO;
import com.xidian.activities.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务实现类 - 使用硅基流动API
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 硅基流动API配置
    @Value("${ai.siliconflow.api-key}")
    private String apiKey;

    @Value("${ai.siliconflow.api-url:https://api.siliconflow.cn/v1/images/generations}")
    private String apiUrl;

    @Value("${ai.siliconflow.model:Qwen/Qwen-Image-Edit-2509}")
    private String model;

    @Override
    public AIImageResultDTO generateActivityPoster(AIPosterGenerateDTO generateDTO) {
        log.info("开始生成AI海报: 活动名称={}", generateDTO.getActivityName());

        try {
            // 1. 构建Prompt
            String prompt = buildPrompt(generateDTO);
            log.info("生成的Prompt: {}", prompt);

            // 2. 调用硅基流动API
            String imageUrl = callSiliconFlowAPI(prompt);
            log.info("AI图片生成成功，URL: {}", imageUrl);

            // 3. 下载图片并转换为Base64
            byte[] imageBytes = downloadImage(imageUrl);
            String base64Data = "data:image/png;base64," +
                    java.util.Base64.getEncoder().encodeToString(imageBytes);

            log.info("图片下载并转换为Base64成功，大小: {} bytes", imageBytes.length);

            // 4. 返回结果（前端自己决定是否上传）
            return new AIImageResultDTO(base64Data, "png", prompt);

        } catch (Exception e) {
            log.error("AI海报生成失败: {}", e.getMessage(), e);
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR,
                    "AI海报生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(AIPosterGenerateDTO dto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("创建一张活动海报，");
        prompt.append("活动名称：").append(dto.getActivityName()).append("，");

        if (dto.getActivityDescription() != null && !dto.getActivityDescription().isEmpty()) {
            prompt.append("活动描述：").append(dto.getActivityDescription()).append("，");
        }

        if (dto.getLocation() != null && !dto.getLocation().isEmpty()) {
            prompt.append("地点：").append(dto.getLocation()).append("，");
        }

        if (dto.getStartTime() != null && !dto.getStartTime().isEmpty()) {
            prompt.append("时间：").append(dto.getStartTime()).append("，");
        }

        // 添加默认设计要求
        prompt.append("海报设计要求：现代简约风格，配色和谐，文字清晰易读，");
        prompt.append("包含活动主题、时间、地点等关键信息，整体布局美观大方");
        prompt.append("CRITICAL: 生成的海报中不允许出现文字，所有关键信息用图标、图案等视觉元素表达");

        // 如果有自定义风格
        if (dto.getStylePrompt() != null && !dto.getStylePrompt().isEmpty()) {
            prompt.append("，").append(dto.getStylePrompt());
        }

        return prompt.toString();
    }

    /**
     * 调用硅基流动API生成图片
     */
    private String callSiliconFlowAPI(String prompt) throws Exception {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        log.info("使用API KEY: {}", apiKey);
        log.info("使用model: {}", model);

        // 构建请求体 - 根据模型类型使用不同参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("num_inference_steps", 20);

        // 根据模型类型设置不同参数
        if (model.contains("Qwen")) {
            // Qwen系列模型使用cfg参数，且不支持image_size
            requestBody.put("cfg", 4.0); // 官方推荐配置：50步，CFG 4.0
            log.info("使用Qwen模型参数: cfg=4.0");
        } else if (model.contains("Kolors")) {
            // Kolors模型使用guidance_scale和image_size
            requestBody.put("image_size", "1024x1024");
            requestBody.put("guidance_scale", 7.5);
            log.info("使用Kolors模型参数: image_size=1024x1024, guidance_scale=7.5");
        } else {
            // FLUX等其他模型
            requestBody.put("image_size", "1024x1024");
            requestBody.put("guidance_scale", 7.5);
            log.info("使用默认模型参数: image_size=1024x1024, guidance_scale=7.5");
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 发送请求
        log.info("调用硅基流动API: {}, model: {}", apiUrl, model);
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("API调用失败: " + response.getStatusCode());
        }

        // 解析响应
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        JsonNode imagesNode = responseNode.get("images");

        if (imagesNode == null || !imagesNode.isArray() || imagesNode.isEmpty()) {
            throw new RuntimeException("API返回数据格式错误");
        }

        JsonNode firstImage = imagesNode.get(0);
        return firstImage.get("url").asText();
    }

    /**
     * 下载图片
     */
    private byte[] downloadImage(String imageUrl) throws Exception {
        log.info("开始下载图片: {}", imageUrl);
        URL url = new URL(imageUrl);

        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }
}
