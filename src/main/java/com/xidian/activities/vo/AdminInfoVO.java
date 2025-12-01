package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员信息VO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员信息")
public class AdminInfoVO {

    @Schema(description = "管理员ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "活动管理员")
    private String realName;

    @Schema(description = "手机号码", example = "13800138001")
    private String phone;

    @Schema(description = "电子邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "角色类型：1-普通管理员, 2-超级管理员", example = "1")
    private Integer roleType;

    @Schema(description = "角色名称", example = "普通管理员")
    private String roleName;

    @Schema(description = "账户状态：0-禁用, 1-启用", example = "1")
    private Integer accountStatus;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}