package com.xidian.activities.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public interface RedisService {

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置缓存并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     * @return 删除的数量
     */
    Long delete(Collection<String> keys);

    /**
     * 判断缓存是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    Long getExpire(String key);

    /**
     * 根据前缀删除缓存
     *
     * @param prefix 前缀
     * @return 删除的数量
     */
    Long deleteByPrefix(String prefix);

    /**
     * Hash设置
     *
     * @param key   键
     * @param hKey  哈希键
     * @param value 值
     */
    void hSet(String key, String hKey, Object value);

    /**
     * Hash获取
     *
     * @param key  键
     * @param hKey 哈希键
     * @return 值
     */
    Object hGet(String key, String hKey);

    /**
     * Hash获取所有
     *
     * @param key 键
     * @return Map
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * Hash删除
     *
     * @param key   键
     * @param hKeys 哈希键集合
     * @return 删除的数量
     */
    Long hDelete(String key, Object... hKeys);

    /**
     * List设置
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long lPush(String key, Object value);

    /**
     * List获取
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 列表
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * Set设置
     *
     * @param key    键
     * @param values 值集合
     * @return 添加的数量
     */
    Long sAdd(String key, Object... values);

    /**
     * Set获取
     *
     * @param key 键
     * @return 集合
     */
    Set<Object> sMembers(String key);

    /**
     * 自增操作
     *
     * @param key 键
     * @return 自增后的值
     */
    Long increment(String key);

    /**
     * 自增指定步长
     *
     * @param key       键
     * @param increment 步长
     * @return 自增后的值
     */
    Long increment(String key, long increment);
}