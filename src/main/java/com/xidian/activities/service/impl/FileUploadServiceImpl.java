package com.xidian.activities.service.impl;

import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.FileUploadResultDTO;
import com.xidian.activities.service.FileUploadService;
import com.xidian.activities.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 *
 * @author
 * @since
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioUtil minioUtil;

    @Value("${minio.bucket-name:activities}")
    private String bucketName;

    @Value("${minio.file-expire-hours:24}")
    private Integer fileExpireHours;

    /**
     * 支持的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    /**
     * 支持的文档类型
     */
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    /**
     * 最大文件大小 (50MB)
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Override
    public FileUploadResultDTO uploadFile(MultipartFile file, String purpose) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "文件大小不能超过50MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!isValidFileType(contentType)) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "不支持的文件类型，仅支持图片和文档");
        }

        try {
            // 生成文件ID和存储文件名
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileId = generateFileId(purpose, fileExtension);
            String objectName = generateObjectName(purpose, fileId, fileExtension);

            // 上传文件到MinIO
            minioUtil.upload(bucketName, objectName, file.getInputStream(), contentType);

            // 构建返回结果
            return FileUploadResultDTO.builder()
                    .fileId(fileId)
                    .originalFileName(originalFileName)
                    .fileName(fileId + fileExtension)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .purpose(purpose)
                    .url(minioUtil.getFileUrl(bucketName, objectName))
                    .extension(fileExtension)
                    .uploadTime(System.currentTimeMillis())
                    .build();

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR, "文件上传失败");
        }
    }

    @Override
    public List<FileUploadResultDTO> uploadFiles(List<MultipartFile> files, String purpose) {
        List<FileUploadResultDTO> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResultDTO result = uploadFile(file, purpose);
                results.add(result);
            } catch (BizException e) {
                log.warn("文件上传失败: {}, 跳过该文件", e.getMessage());
                // 批量上传中单个文件失败不影响其他文件
            }
        }

        return results;
    }

    @Override
    public String getFileUrl(String fileId) {
        try {
            // 根据fileId构建objectName (这里简化处理，实际需要存储映射关系)
            String objectName = fileId.startsWith("activity_cover_") ? "activity_cover/" + fileId : "general/" + fileId;

            // 返回公开永久URL
            return minioUtil.getFileUrl(bucketName, objectName);
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR, "获取文件访问链接失败");
        }
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            // 根据fileId构建objectName
            String objectName = fileId.startsWith("activity_cover_") ? "activity_cover/" + fileId : "general/" + fileId;

            minioUtil.removeObject(bucketName, objectName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR, "文件删除失败");
        }
    }

    @Override
    public FileUploadResultDTO uploadBase64(String base64Data, String fileName, String purpose) {
        try {
            // 解析Base64数据
            byte[] fileData;
            String contentType;

            if (base64Data.contains("base64,")) {
                // 处理带数据头的Base64 (如: data:image/jpeg;base64,xxx)
                String[] parts = base64Data.split(",");
                if (parts.length != 2) {
                    throw BizException.of(ResultCodeEnum.DATA_ERROR, "Base64数据格式错误");
                }
                fileData = Base64.getDecoder().decode(parts[1]);
                contentType = parts[0].split(";")[0].split(":")[1];
            } else {
                // 处理纯Base64数据
                fileData = Base64.getDecoder().decode(base64Data);
                contentType = "image/jpeg"; // 默认为jpeg
            }

            // 检查文件大小
            if (fileData.length > MAX_FILE_SIZE) {
                throw BizException.of(ResultCodeEnum.DATA_ERROR, "文件大小不能超过50MB");
            }

            // 生成文件ID和存储文件名
            String fileExtension = getFileExtension(fileName);
            String fileId = generateFileId(purpose, fileExtension);
            String objectName = generateObjectName(purpose, fileId, fileExtension);

            // 上传文件到MinIO
            minioUtil.upload(bucketName, objectName, new ByteArrayInputStream(fileData), contentType);

            // 构建返回结果
            return FileUploadResultDTO.builder()
                    .fileId(fileId)
                    .originalFileName(fileName)
                    .fileName(fileId + fileExtension)
                    .fileSize((long) fileData.length)
                    .contentType(contentType)
                    .purpose(purpose)
                    .url(minioUtil.getFileUrl(bucketName, objectName))
                    .extension(fileExtension)
                    .uploadTime(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Base64文件上传失败: {}", e.getMessage(), e);
            throw BizException.of(ResultCodeEnum.INTERNAL_SERVER_ERROR, "Base64文件上传失败");
        }
    }

    /**
     * 检查文件类型是否有效
     */
    private boolean isValidFileType(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType) || ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    /**
     * 生成文件ID
     */
    private String generateFileId(String purpose, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return purpose + "_" + timestamp + "_" + uuid;
    }

    /**
     * 生成对象存储名称
     */
    private String generateObjectName(String purpose, String fileId, String extension) {
        return purpose + "/" + fileId + extension;
    }
}