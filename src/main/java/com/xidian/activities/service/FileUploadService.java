package com.xidian.activities.service;

import com.xidian.activities.dto.FileUploadResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 *
 * @author
 * @since
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     *
     * @param file    文件
     * @param purpose 用途
     * @return 上传结果
     */
    FileUploadResultDTO uploadFile(MultipartFile file, String purpose);

    /**
     * 批量上传文件
     *
     * @param files   文件列表
     * @param purpose 用途
     * @return 上传结果列表
     */
    List<FileUploadResultDTO> uploadFiles(List<MultipartFile> files, String purpose);

    /**
     * 获取文件访问URL
     *
     * @param fileId 文件ID
     * @return 访问URL
     */
    String getFileUrl(String fileId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    void deleteFile(String fileId);

    /**
     * Base64文件上传
     *
     * @param base64Data Base64编码数据
     * @param fileName   文件名
     * @param purpose    用途
     * @return 上传结果
     */
    FileUploadResultDTO uploadBase64(String base64Data, String fileName, String purpose);
}