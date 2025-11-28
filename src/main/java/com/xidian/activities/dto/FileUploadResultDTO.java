package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件上传结果DTO
 *
 * @author
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传结果")
public class FileUploadResultDTO {

    @Schema(description = "文件ID", example = "activity_cover_20231215_001")
    private String fileId;

    @Schema(description = "原始文件名", example = "活动封面.jpg")
    private String originalFileName;

    @Schema(description = "存储文件名", example = "activity_cover_20231215_001.jpg")
    private String fileName;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "文件类型", example = "image/jpeg")
    private String contentType;

    @Schema(description = "文件用途", example = "activity_cover")
    private String purpose;

    @Schema(description = "访问URL（永久有效）", example = "http://localhost:9000/activities/posters/xxx.jpg")
    private String url;

    @Schema(description = "文件扩展名", example = ".jpg")
    private String extension;

    @Schema(description = "上传时间戳", example = "1702588800000")
    private Long uploadTime;
}