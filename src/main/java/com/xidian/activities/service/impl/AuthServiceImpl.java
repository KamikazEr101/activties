package com.xidian.activities.service.impl;

import com.xidian.activities.common.login.LoginUserHolder;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.constant.CacheConstants;
import com.xidian.activities.dto.AdminLoginDTO;
import com.xidian.activities.mapper.AdministratorMapper;
import com.xidian.activities.entity.Administrator;
import com.xidian.activities.service.AuthService;
import com.xidian.activities.service.RedisService;
import com.xidian.activities.util.BCryptUtil;
import com.xidian.activities.util.JwtUtil;
import com.xidian.activities.vo.AdminInfoVO;
import com.xidian.activities.vo.AdminLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * @author
 * @since
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Override
    @Transactional
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        // 根据用户名查询管理员
        Administrator administrator = administratorMapper.selectByUsername(loginDTO.getUsername());
        if (administrator == null) {
            throw new BizException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 检查账户状态
        if (administrator.getAccountStatus() == 0) {
            throw new BizException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        // 验证密码（使用BCrypt加密比对）
        if (!BCryptUtil.verifyPassword(loginDTO.getPassword(), administrator.getPassword())) {
            throw new BizException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        // 更新最后登录时间
        administrator.setLastLoginTime(LocalDateTime.now());
        administratorMapper.updateById(administrator);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(
                administrator.getUsername(),
                administrator.getId(),
                administrator.getRoleType());

        // 将Token存储到Redis中
        String tokenKey = CacheConstants.USER_TOKEN_CACHE + token;
        redisService.set(tokenKey, administrator.getId(), CacheConstants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        // 构建响应
        return AdminLoginVO.builder()
                .token(token)
                .tokenType(jwtUtil.getTokenHead().trim())
                .adminId(administrator.getId())
                .username(administrator.getUsername())
                .realName(administrator.getRealName())
                .roleType(administrator.getRoleType())
                .roleName(administrator.getRoleType() == 2 ? "超级管理员" : "普通管理员")
                .expiresIn(jwtUtil.getExpiration())
                .build();
    }

    @Override
    public AdminInfoVO getCurrentAdminInfo() {
        Long adminId = LoginUserHolder.getAdminId();
        if (adminId == null) {
            throw new BizException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        Administrator administrator = administratorMapper.selectById(adminId);
        if (administrator == null) {
            throw new BizException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        AdminInfoVO adminInfoVO = new AdminInfoVO();
        BeanUtils.copyProperties(administrator, adminInfoVO);
        adminInfoVO.setRoleName(administrator.getRoleType() == 2 ? "超级管理员" : "普通管理员");

        return adminInfoVO;
    }

    @Override
    public void logout() {
        // 获取当前Token
        String currentToken = LoginUserHolder.getCurrentToken();
        if (currentToken != null) {
            // 从Redis中删除Token
            String tokenKey = CacheConstants.USER_TOKEN_CACHE + currentToken;
            redisService.delete(tokenKey);
        }

        // 清除用户上下文
        LoginUserHolder.clear();
        log.info("管理员登出成功");
    }
}