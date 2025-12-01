package com.xidian.activities.service.impl;

import com.xidian.activities.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis设置缓存成功: key = {}", key);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key = {}, error = {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("Redis设置缓存成功: key = {}, timeout = {} {}", key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key = {}, error = {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Redis获取缓存: key = {}, value = {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis获取缓存失败: key = {}, error = {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Redis删除缓存: key = {}, result = {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis删除缓存失败: key = {}, error = {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Long delete(Collection<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            log.debug("Redis批量删除缓存: keys = {}, count = {}", keys, result);
            return result;
        } catch (Exception e) {
            log.error("Redis批量删除缓存失败: keys = {}, error = {}", keys, e.getMessage());
            return 0L;
        }
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            log.debug("Redis检查键是否存在: key = {}, exists = {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis检查键是否存在失败: key = {}, error = {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, timeUnit);
            log.debug("Redis设置过期时间: key = {}, timeout = {} {}, result = {}", key, timeout, timeUnit, result);
            return result;
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: key = {}, error = {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            Long result = redisTemplate.getExpire(key);
            log.debug("Redis获取过期时间: key = {}, expire = {}s", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis获取过期时间失败: key = {}, error = {}", key, e.getMessage());
            return -1L;
        }
    }

    @Override
    public Long deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (!keys.isEmpty()) {
                Long result = redisTemplate.delete(keys);
                log.debug("Redis根据前缀删除缓存: prefix = {}, count = {}", prefix, result);
                return result;
            }
            return 0L;
        } catch (Exception e) {
            log.error("Redis根据前缀删除缓存失败: prefix = {}, error = {}", prefix, e.getMessage());
            return 0L;
        }
    }

    @Override
    public void hSet(String key, String hKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hKey, value);
            log.debug("Redis Hash设置: key = {}, hKey = {}, value = {}", key, hKey, value);
        } catch (Exception e) {
            log.error("Redis Hash设置失败: key = {}, hKey = {}, error = {}", key, hKey, e.getMessage());
            throw e;
        }
    }

    @Override
    public Object hGet(String key, String hKey) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hKey);
            log.debug("Redis Hash获取: key = {}, hKey = {}, value = {}", key, hKey, value);
            return value;
        } catch (Exception e) {
            log.error("Redis Hash获取失败: key = {}, hKey = {}, error = {}", key, hKey, e.getMessage());
            return null;
        }
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        try {
            Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
            log.debug("Redis Hash获取所有: key = {}, size = {}", key, result.size());
            return result;
        } catch (Exception e) {
            log.error("Redis Hash获取所有失败: key = {}, error = {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Long hDelete(String key, Object... hKeys) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, hKeys);
            log.debug("Redis Hash删除: key = {}, hKeys = {}, count = {}", key, hKeys, result);
            return result;
        } catch (Exception e) {
            log.error("Redis Hash删除失败: key = {}, hKeys = {}, error = {}", key, hKeys, e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long lPush(String key, Object value) {
        try {
            Long result = redisTemplate.opsForList().leftPush(key, value);
            log.debug("Redis List设置: key = {}, value = {}, length = {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Redis List设置失败: key = {}, error = {}", key, e.getMessage());
            return 0L;
        }
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        try {
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            log.debug("Redis List获取: key = {}, range = {}-{}, size = {}", key, start, end,
                    result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Redis List获取失败: key = {}, error = {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Long sAdd(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().add(key, values);
            log.debug("Redis Set设置: key = {}, values = {}, count = {}", key, values, result);
            return result;
        } catch (Exception e) {
            log.error("Redis Set设置失败: key = {}, error = {}", key, e.getMessage());
            return 0L;
        }
    }

    @Override
    public Set<Object> sMembers(String key) {
        try {
            Set<Object> result = redisTemplate.opsForSet().members(key);
            log.debug("Redis Set获取: key = {}, size = {}", key, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Redis Set获取失败: key = {}, error = {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Long increment(String key) {
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            log.debug("Redis自增: key = {}, result = {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自增失败: key = {}, error = {}", key, e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long increment(String key, long increment) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, increment);
            log.debug("Redis自增: key = {}, increment = {}, result = {}", key, increment, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自增失败: key = {}, increment = {}, error = {}", key, increment, e.getMessage());
            return 0L;
        }
    }
}