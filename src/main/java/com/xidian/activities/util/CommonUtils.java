package com.xidian.activities.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 通用工具类
 *
 * @author
 * @since
 */
@Slf4j
public class CommonUtils {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9][0-9]{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 检查手机号格式是否正确
     *
     * @param phone 手机号
     * @return 是否正确
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 检查邮箱格式是否正确
     *
     * @param email 邮箱
     * @return 是否正确
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 生成UUID
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 格式化日期时间
     *
     * @param localDateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期
     *
     * @param localDateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(DATE_FORMATTER);
    }

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

    /**
     * 安全地将字符串转换为整数
     *
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 整数
     */
    public static int safeParseInt(String str, int defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析整数: {}, 使用默认值: {}", str, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全地将字符串转换为长整数
     *
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 长整数
     */
    public static long safeParseLong(String str, long defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析长整数: {}, 使用默认值: {}", str, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 生成文件名
     *
     * @param originalName 原始文件名
     * @param prefix 前缀
     * @return 生成的文件名
     */
    public static String generateFileName(String originalName, String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = generateUUID().substring(0, 8);
        String extension = "";

        if (isNotBlank(originalName)) {
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalName.substring(lastDotIndex);
            }
        }

        return String.format("%s_%s_%s%s", prefix, timestamp, uuid, extension);
    }

    /**
     * 脱敏手机号
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (isBlank(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (isBlank(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 3) {
            return username.charAt(0) + "***" + domain;
        }

        return username.substring(0, 3) + "***" + domain;
    }
}