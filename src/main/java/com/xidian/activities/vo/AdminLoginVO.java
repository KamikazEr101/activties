package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员登录响应VO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员登录响应")
public class AdminLoginVO {

    @Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "管理员ID", example = "1")
    private Long adminId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "活动管理员")
    private String realName;

    @Schema(description = "角色类型", example = "1")
    private Integer roleType;

    @Schema(description = "角色名称", example = "普通管理员")
    private String roleName;

    @Schema(description = "令牌过期时间（毫秒）", example = "3600000")
    private Long expiresIn;
}