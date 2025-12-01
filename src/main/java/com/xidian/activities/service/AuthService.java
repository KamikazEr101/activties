package com.xidian.activities.service;

import com.xidian.activities.dto.AdminLoginDTO;
import com.xidian.activities.vo.AdminLoginVO;
import com.xidian.activities.vo.AdminInfoVO;

/**
 * 认证服务接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public interface AuthService {

    /**
     * 管理员登录
     *
     * @param loginDTO 登录请求
     * @return 登录响应
     */
    AdminLoginVO login(AdminLoginDTO loginDTO);

    /**
     * 获取当前管理员信息
     *
     * @return 管理员信息
     */
    AdminInfoVO getCurrentAdminInfo();

    /**
     * 管理员登出
     */
    void logout();
}