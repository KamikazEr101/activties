package com.xidian.activities.constant;

/**
 * 缓存常量
 *
 * @author
 * @since
 */
public interface CacheConstants {

    /**
     * 缓存命名空间
     */
    String CACHE_NAMESPACE = "activities:";

    /**
     * 活动缓存
     */
    String ACTIVITY_CACHE = CACHE_NAMESPACE + "activity:";

    /**
     * 活动列表缓存
     */
    String ACTIVITY_LIST_CACHE = CACHE_NAMESPACE + "activity:list:";

    /**
     * 管理员缓存
     */
    String ADMIN_CACHE = CACHE_NAMESPACE + "admin:";

    /**
     * 用户Token缓存
     */
    String USER_TOKEN_CACHE = CACHE_NAMESPACE + "token:";

    /**
     * 验证码缓存
     */
    String CAPTCHA_CACHE = CACHE_NAMESPACE + "captcha:";

    /**
     * 文件上传缓存
     */
    String FILE_UPLOAD_CACHE = CACHE_NAMESPACE + "file_upload:";

    /**
     * 系统配置缓存
     */
    String SYSTEM_CONFIG_CACHE = CACHE_NAMESPACE + "system_config:";

    /**
     * 活动访问统计缓存
     */
    String ACTIVITY_VIEW_COUNT_CACHE = CACHE_NAMESPACE + "activity:view:";

    /**
     * 默认过期时间（秒）
     */
    long DEFAULT_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 短期过期时间（秒）
     */
    long SHORT_EXPIRE_TIME = 300; // 5分钟

    /**
     * 长期过期时间（秒）
     */
    long LONG_EXPIRE_TIME = 86400; // 24小时

    /**
     * Token过期时间（秒）
     */
    long TOKEN_EXPIRE_TIME = 3600; // 1小时
}