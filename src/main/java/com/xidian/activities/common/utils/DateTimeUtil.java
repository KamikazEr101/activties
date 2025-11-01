package com.xidian.activities.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateTimeUtil {
    
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    
    /**
     * 获取当前日期时间字符串
     */
    public static String now() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }
    
    /**
     * 获取当前日期字符串
     */
    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    /**
     * 获取当前时间字符串
     */
    public static String currentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
    
    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    /**
     * 格式化日期时间（自定义格式）
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * 格式化时间
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }
    
    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }
    
    /**
     * 解析日期时间字符串（自定义格式）
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    /**
     * 解析时间字符串
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }
    
    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 计算两个时间之间的天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * 计算两个时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startTime, endTime);
    }
    
    /**
     * 计算两个时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }
    
    /**
     * 判断时间是否在指定范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (dateTime == null || startTime == null || endTime == null) {
            return false;
        }
        return !dateTime.isBefore(startTime) && !dateTime.isAfter(endTime);
    }
    
    /**
     * 判断当前时间是否在指定范围内
     */
    public static boolean isNowBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return isBetween(LocalDateTime.now(), startTime, endTime);
    }
    
    /**
     * 判断日期是否是今天
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }
    
    /**
     * 判断日期是否是昨天
     */
    public static boolean isYesterday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now().minusDays(1));
    }
    
    /**
     * 判断日期是否是明天
     */
    public static boolean isTomorrow(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now().plusDays(1));
    }
    
    /**
     * 获取指定日期的开始时间（00:00:00）
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }
    
    /**
     * 获取指定日期的结束时间（23:59:59）
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }
    
    /**
     * 获取当前日期的开始时间
     */
    public static LocalDateTime getStartOfToday() {
        return getStartOfDay(LocalDate.now());
    }
    
    /**
     * 获取当前日期的结束时间
     */
    public static LocalDateTime getEndOfToday() {
        return getEndOfDay(LocalDate.now());
    }
    
    /**
     * 添加天数
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }
    
    /**
     * 添加小时
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
    
    /**
     * 添加分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }
    
    /**
     * 获取指定时间的友好描述
     */
    public static String getFriendlyTimeDescription(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = minutesBetween(dateTime, now);
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) { // 24小时
            long hours = minutes / 60;
            return hours + "小时前";
        } else if (minutes < 10080) { // 7天
            long days = minutes / 1440;
            return days + "天前";
        } else {
            return format(dateTime);
        }
    }
}