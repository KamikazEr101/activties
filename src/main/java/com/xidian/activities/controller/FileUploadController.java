package com.xidian.activities.controller;

import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.FileUploadResultDTO;
import com.xidian.activities.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 *
 * @author
 * @since
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件上传相关接口")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private com.xidian.activities.service.AIService aiService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件到MinIO存储")
    public Result<FileUploadResultDTO> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "文件用途", example = "activity_cover") @RequestParam(value = "purpose", defaultValue = "general") String purpose) {

        log.info("文件上传请求: 文件名 {}, 用途 {}, 大小 {}",
                file.getOriginalFilename(), purpose, file.getSize());

        FileUploadResultDTO result = fileUploadService.uploadFile(file, purpose);
        log.info("文件上传成功: 文件ID {}, 文件名 {}", result.getFileId(), result.getFileName());

        return Result.ok(result);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public Result<List<FileUploadResultDTO>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true) @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "文件用途", example = "activity_cover") @RequestParam(value = "purpose", defaultValue = "general") String purpose) {

        log.info("批量文件上传请求: 文件数量 {}, 用途 {}", files.size(), purpose);

        List<FileUploadResultDTO> results = fileUploadService.uploadFiles(files, purpose);
        log.info("批量文件上传成功: 上传数量 {}", results.size());

        return Result.ok(results);
    }

    @GetMapping("/url/{fileId}")
    @Operation(summary = "获取文件访问URL", description = "根据文件ID获取永久访问链接")
    public Result<String> getFileUrl(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId) {

        log.info("获取文件URL请求: 文件ID {}", fileId);

        String fileUrl = fileUploadService.getFileUrl(fileId);
        log.info("文件URL获取成功: 文件ID {}", fileId);

        return Result.ok(fileUrl);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "删除指定文件")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId) {

        log.info("删除文件请求: 文件ID {}", fileId);

        fileUploadService.deleteFile(fileId);
        log.info("文件删除成功: 文件ID {}", fileId);

        return Result.ok();
    }

    @PostMapping("/upload/base64")
    @Operation(summary = "Base64文件上传", description = "通过Base64编码上传文件")
    public Result<FileUploadResultDTO> uploadBase64(
            @Parameter(description = "Base64编码的文件数据", required = true) @RequestBody String base64Data,

            @Parameter(description = "文件名", example = "cover.jpg") @RequestParam String fileName,

            @Parameter(description = "文件用途", example = "activity_cover") @RequestParam(value = "purpose", defaultValue = "general") String purpose) {

        log.info("Base64文件上传请求: 文件名 {}, 用途 {}, 数据大小 {}",
                fileName, purpose, base64Data.length());

        FileUploadResultDTO result = fileUploadService.uploadBase64(base64Data, fileName, purpose);
        log.info("Base64文件上传成功: 文件ID {}", result.getFileId());

        return Result.ok(result);
    }

    @PostMapping("/ai/generate-poster")
    @Operation(summary = "AI生成活动海报", description = "根据活动信息使用AI生成海报图片并返回Base64")
    public Result<com.xidian.activities.dto.AIImageResultDTO> generateAIPoster(
            @jakarta.validation.Valid @RequestBody com.xidian.activities.dto.AIPosterGenerateDTO generateDTO) {

        log.info("AI生成海报请求: 活动名称={}", generateDTO.getActivityName());

        com.xidian.activities.dto.AIImageResultDTO result = aiService.generateActivityPoster(generateDTO);
        log.info("AI海报生成成功");

        return Result.ok(result);
    }

}