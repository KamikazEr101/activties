package com.xidian.activities.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 管理员表 (administrators)
 */
@Data
public class Administrator {
    /**
     * 管理员ID，主键
     */
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 角色：1-普通管理员, 2-超级管理员
     */
    private Integer roleType;

    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer accountStatus;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 软删除标记
     */
    private Integer isDeleted;

    /**
     * 记录创建时间
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;
}
