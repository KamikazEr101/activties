package com.xidian.activities.interceptor;

import com.xidian.activities.common.login.LoginUser;
import com.xidian.activities.common.login.LoginUserHolder;
import com.xidian.activities.common.result.Result;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器
 * 用于验证JWT令牌和设置用户上下文
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 检查是否是OPTIONS请求（跨域预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(jwtUtil.getTokenHead())) {
            log.warn("请求缺少有效的Authorization头: {}", request.getRequestURI());
            sendErrorResponse(response, ResultCodeEnum.UNAUTHORIZED);
            return false;
        }

        // 提取JWT令牌
        String token = authHeader.substring(jwtUtil.getTokenHead().length());
        try {
            // 验证令牌是否有效
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("JWT令牌已过期: {}", request.getRequestURI());
                sendErrorResponse(response, ResultCodeEnum.TOKEN_EXPIRED);
                return false;
            }

            // 从令牌中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            Long adminId = jwtUtil.getAdminIdFromToken(token);
            Integer roleType = jwtUtil.getRoleTypeFromToken(token);

            // 验证令牌是否有效（额外验证）
            if (!jwtUtil.validateToken(token, username)) {
                log.warn("JWT令牌验证失败: {}", request.getRequestURI());
                sendErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
                return false;
            }

            // 设置用户上下文
            LoginUser loginUser = LoginUser.builder()
                    .adminId(adminId)
                    .username(username)
                    .roleType(roleType)
                    .roleName(roleType == 2 ? "超级管理员" : "普通管理员")
                    .token(token)
                    .build();

            LoginUserHolder.setLoginUser(loginUser);
            log.debug("用户认证成功: {}, 角色: {}", username, loginUser.getRoleName());

            return true;

        } catch (Exception e) {
            log.error("JWT令牌处理异常: {}", e.getMessage());
            sendErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
            return false;
        }
    }

    @Override
    public void afterCompletion(@SuppressWarnings("NullableProblems") HttpServletRequest request, @SuppressWarnings("NullableProblems") HttpServletResponse response, @SuppressWarnings("NullableProblems") Object handler, Exception ex) {
        // 清除用户上下文
        LoginUserHolder.clear();
    }

    /**
     * 发送错误响应
     *
     * @param response   HTTP响应
     * @param resultCode 错误码
     * @throws IOException IO异常
     */
    private void sendErrorResponse(HttpServletResponse response, ResultCodeEnum resultCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.fail(resultCode);
        String jsonResponse = objectMapper.writeValueAsString(result);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
