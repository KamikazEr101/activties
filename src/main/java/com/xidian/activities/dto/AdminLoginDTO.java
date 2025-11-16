package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员登录请求DTO
 *
 * @author
 * @since
 */
@Data
@Schema(description = "管理员登录请求")
public class AdminLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    @Schema(description = "用户名", example = "admin", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100字符之间")
    @Schema(description = "密码", example = "admin123", required = true)
    private String password;
}