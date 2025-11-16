package com.xidian.activities.common.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息
 *
 * @author
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色类型：1-普通管理员, 2-超级管理员
     */
    private Integer roleType;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * JWT令牌
     */
    private String token;
}