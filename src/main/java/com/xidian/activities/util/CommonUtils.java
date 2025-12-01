package com.xidian.activities.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用工具类
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
public class CommonUtils {

    /**
     * 字符串是否为空或空白
     *
     * @param str 字符串
     * @return 是否为空或空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 字符串是否不为空且不为空白
     *
     * @param str 字符串
     * @return 是否不为空且不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}