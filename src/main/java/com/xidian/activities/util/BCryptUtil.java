package com.xidian.activities.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * BCrypt加密工具类
 *
 * @author
 * @since
 */
public class BCryptUtil {

    /**
     * 默认加密强度
     */
    private static final int DEFAULT_COST = 12;

    /**
     * 对密码进行BCrypt加密
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(DEFAULT_COST, plainPassword.toCharArray());
    }

    /**
     * 验证密码是否匹配
     *
     * @param plainPassword  明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
