package com.xidian.activities.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类 (administrators)
 *
 * @author
 * @since
 */
@Data
public class Administrator {

    /**
     * 管理员ID，主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 角色：1-普通管理员, 2-超级管理员
     */
    private Integer roleType;

    /**
     * 账户状态：0-正常, 1-禁用
     */
    private Integer accountStatus;

    /**
     * 软删除标记：0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}