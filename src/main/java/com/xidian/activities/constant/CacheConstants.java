package com.xidian.activities.constant;

/**
 * 缓存常量
 *
 * @author KamikazEr101
 * @since 2025/11/20
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
     * 用户Token缓存
     */
    String USER_TOKEN_CACHE = CACHE_NAMESPACE + "token:";

    /**
     * 活动访问统计缓存
     */
    String ACTIVITY_VIEW_COUNT_CACHE = CACHE_NAMESPACE + "activity:view:";

    /**
     * 默认过期时间（秒）
     */
    long DEFAULT_EXPIRE_TIME = 1800; // 30分钟

    /**
     * Token过期时间（秒）
     */
    long TOKEN_EXPIRE_TIME = 3600; // 1小时
}