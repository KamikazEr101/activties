package com.xidian.activities.constant;

/**
 * 通用常量类
 *
 * @author
 * @since
 */
public class CommonConstants {

    /**
     * 通用状态常量
     */
    public static class Status {
        public static final Integer ENABLE = 0;
        public static final Integer DISABLE = 1;
        public static final Integer NOT_DELETED = 0;
        public static final Integer DELETED = 1;
    }

    /**
     * 活动状态常量
     */
    public static class ActivityStatus {
        public static final Integer DRAFT = 0;        // 草稿
        public static final Integer REGISTRATION = 1; // 报名中
        public static final Integer REGISTRATION_ENDED = 2; // 报名结束
        public static final Integer IN_PROGRESS = 3;  // 进行中
        public static final Integer ENDED = 4;        // 已结束
        public static final Integer CANCELLED = 5;    // 已取消
    }

    /**
     * 报名状态常量
     */
    public static class RegistrationStatus {
        public static final Integer SUCCESS = 1;      // 报名成功
        public static final Integer CANCELLED = 2;    // 已取消
    }

    /**
     * 签到状态常量
     */
    public static class CheckInStatus {
        public static final Integer NOT_CHECKED_IN = 0; // 未签到
        public static final Integer CHECKED_IN = 1;     // 已签到
    }

    /**
     * 管理员角色常量
     */
    public static class AdminRole {
        public static final Integer SUPER_ADMIN = 1;  // 超级管理员
        public static final Integer ADMIN = 2;        // 普通管理员
    }

    /**
     * 管理员账户状态常量
     */
    public static class AdminAccountStatus {
        public static final Integer NORMAL = 0;    // 正常
        public static final Integer DISABLED = 1;  // 禁用
    }

    /**
     * JWT相关常量
     */
    public static class Jwt {
        public static final String TOKEN_HEADER = "Authorization";
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String CLAIM_USER_ID = "userId";
        public static final String CLAIM_USERNAME = "username";
        public static final String CLAIM_ROLE = "role";
    }

    /**
     * Redis相关常量
     */
    public static class Redis {
        public static final String LOGIN_TOKEN_PREFIX = "login:token:";
        public static final String ACTIVITY_CACHE_PREFIX = "activity:cache:";
        public static final String ACTIVITY_LIST_PREFIX = "activity:list:";
        public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60; // 24小时
        public static final long CACHE_EXPIRE_TIME = 30 * 60; // 30分钟
    }

    /**
     * 文件相关常量
     */
    public static class File {
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final String[] ALLOWED_IMAGE_TYPES = {
                "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
        };
        public static final String[] ALLOWED_DOCUMENT_TYPES = {
                "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
    }

    /**
     * 时间相关常量
     */
    public static class Time {
        public static final long ONE_MINUTE = 60 * 1000;
        public static final long ONE_HOUR = 60 * ONE_MINUTE;
        public static final long ONE_DAY = 24 * ONE_HOUR;
    }

    /**
     * 分页相关常量
     */
    public static class Page {
        public static final int DEFAULT_PAGE_NUM = 1;
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;
    }

    /**
     * 活动状态流转规则
     */
    public static class ActivityStatusFlow {
        // 草稿状态可以流转到
        public static final Integer[] FROM_DRAFT = {ActivityStatus.REGISTRATION, ActivityStatus.CANCELLED};
        // 报名中状态可以流转到
        public static final Integer[] FROM_REGISTRATION = {ActivityStatus.REGISTRATION_ENDED, ActivityStatus.IN_PROGRESS, ActivityStatus.CANCELLED};
        // 报名结束状态可以流转到
        public static final Integer[] FROM_REGISTRATION_ENDED = {ActivityStatus.IN_PROGRESS, ActivityStatus.CANCELLED};
        // 进行中状态可以流转到
        public static final Integer[] FROM_IN_PROGRESS = {ActivityStatus.ENDED, ActivityStatus.CANCELLED};
        // 已结束状态为最终状态，不能流转
        // 已取消状态为最终状态，不能流转
    }
}