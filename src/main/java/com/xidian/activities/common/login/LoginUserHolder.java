package com.xidian.activities.common.login;

/**
 * 登录用户上下文持有者
 * 用于存储当前线程的登录用户信息
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public class LoginUserHolder {
    private static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     *
     * @param loginUser 登录用户信息
     */
    public static void setLoginUser(LoginUser loginUser) {
        threadLocal.set(loginUser);
    }

    /**
     * 获取当前登录用户
     *
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser() {
        return threadLocal.get();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getAdminId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getAdminId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色类型
     *
     * @return 角色类型
     */
    public static Integer getRoleType() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getRoleType() : null;
    }

    /**
     * 获取当前JWT令牌
     *
     * @return JWT令牌
     */
    public static String getCurrentToken() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getToken() : null;
    }

    /**
     * 清除当前登录用户信息
     */
    public static void clear() {
        threadLocal.remove();
    }
}