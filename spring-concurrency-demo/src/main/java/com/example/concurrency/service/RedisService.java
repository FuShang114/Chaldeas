package com.example.concurrency.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis服务类 - 支持降级模式
 * 当Redis不可用时，自动降级到内存存储
 */
@Service
public class RedisService {
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired(required = false)
    private RedisTemplate<Object, Object> redisTemplate;
    
    // 内存存储降级实现
    private final Map<String, String> memoryStore = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryExpire = new ConcurrentHashMap<>();
    private final AtomicLong lockCounter = new AtomicLong(0);
    
    private volatile boolean redisAvailable = false;
    
    /**
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable() {
        if (!redisAvailable && stringRedisTemplate != null) {
            try {
                stringRedisTemplate.getConnectionFactory().getConnection().ping();
                redisAvailable = true;
                System.out.println("Redis连接正常");
            } catch (Exception e) {
                redisAvailable = false;
                System.err.println("Redis不可用，切换到内存存储模式");
            }
        }
        return redisAvailable && stringRedisTemplate != null;
    }
    
    /**
     * 设置键值对
     */
    public void set(String key, String value, long expireTime) {
        if (isRedisAvailable()) {
            try {
                stringRedisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                System.err.println("Redis设置失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        memoryStore.put(key, value);
        if (expireTime > 0) {
            memoryExpire.put(key, System.currentTimeMillis() + expireTime * 1000);
        }
    }
    
    /**
     * 获取键值
     */
    public String get(String key) {
        if (isRedisAvailable()) {
            try {
                return stringRedisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                System.err.println("Redis获取失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        String value = memoryStore.get(key);
        if (value != null) {
            Long expireTime = memoryExpire.get(key);
            if (expireTime != null && System.currentTimeMillis() > expireTime) {
                memoryStore.remove(key);
                memoryExpire.remove(key);
                return null;
            }
        }
        return value;
    }
    
    /**
     * 删除键
     */
    public void delete(String key) {
        if (isRedisAvailable()) {
            try {
                stringRedisTemplate.delete(key);
                return;
            } catch (Exception e) {
                System.err.println("Redis删除失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        memoryStore.remove(key);
        memoryExpire.remove(key);
    }
    
    /**
     * 检查键是否存在
     */
    public boolean exists(String key) {
        if (isRedisAvailable()) {
            try {
                return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
            } catch (Exception e) {
                System.err.println("Redis检查失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        String value = get(key); // 也会检查过期时间
        return value != null;
    }
    
    /**
     * 尝试获取分布式锁
     * 在内存模式下使用简单的计数器实现
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTime) {
        if (isRedisAvailable()) {
            try {
                Boolean result = stringRedisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                System.err.println("Redis获取锁失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        
        // 降级到内存存储实现分布式锁
        String currentValue = memoryStore.get(lockKey);
        if (currentValue == null) {
            // 锁未被占用，获取锁
            memoryStore.put(lockKey, lockValue);
            memoryExpire.put(lockKey, System.currentTimeMillis() + expireTime);
            return true;
        }
        
        // 检查锁是否过期
        Long expireAt = memoryExpire.get(lockKey);
        if (expireAt != null && System.currentTimeMillis() > expireAt) {
            // 锁已过期，尝试重新获取
            memoryStore.remove(lockKey);
            memoryExpire.remove(lockKey);
            return tryLock(lockKey, lockValue, expireTime);
        }
        
        return false;
    }
    
    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        if (isRedisAvailable()) {
            try {
                String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
                if (lockValue.equals(currentValue)) {
                    stringRedisTemplate.delete(lockKey);
                    return true;
                }
                return false;
            } catch (Exception e) {
                System.err.println("Redis释放锁失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        
        // 降级到内存存储实现
        String currentValue = memoryStore.get(lockKey);
        if (lockValue.equals(currentValue)) {
            memoryStore.remove(lockKey);
            memoryExpire.remove(lockKey);
            return true;
        }
        return false;
    }
    
    /**
     * 获取分布式锁的值
     */
    public String getLockValue(String lockKey) {
        return get(lockKey);
    }
    
    /**
     * 设置缓存（Object类型）
     */
    public void setCache(String key, Object value, long expireTime) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                System.err.println("Redis设置缓存失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        memoryStore.put(key, String.valueOf(value));
        if (expireTime > 0) {
            memoryExpire.put(key, System.currentTimeMillis() + expireTime * 1000);
        }
    }
    
    /**
     * 获取缓存（Object类型）
     */
    public Object getCache(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                System.err.println("Redis获取缓存失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        return get(key);
    }
    
    /**
     * 批量设置缓存
     */
    public void mset(Map<String, String> keyValuePairs, long expireTime) {
        if (isRedisAvailable()) {
            try {
                stringRedisTemplate.opsForValue().multiSet(keyValuePairs);
                if (expireTime > 0) {
                    keyValuePairs.keySet().forEach(key -> 
                        stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS));
                }
                return;
            } catch (Exception e) {
                System.err.println("Redis批量设置失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        keyValuePairs.forEach((key, value) -> set(key, value, expireTime));
    }
    
    /**
     * 批量获取缓存
     */
    public List<String> mget(List<String> keys) {
        if (isRedisAvailable()) {
            try {
                return stringRedisTemplate.opsForValue().multiGet(keys);
            } catch (Exception e) {
                System.err.println("Redis批量获取失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        return keys.stream().map(this::get).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 增加计数器
     */
    public long incr(String key, long delta) {
        if (isRedisAvailable()) {
            try {
                return stringRedisTemplate.opsForValue().increment(key, delta);
            } catch (Exception e) {
                System.err.println("Redis计数器失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        String current = get(key);
        long currentValue = current != null ? Long.parseLong(current) : 0;
        long newValue = currentValue + delta;
        set(key, String.valueOf(newValue), 0);
        return newValue;
    }
    
    /**
     * 设置过期时间
     */
    public boolean expire(String key, long expireTime) {
        if (isRedisAvailable()) {
            try {
                return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS));
            } catch (Exception e) {
                System.err.println("Redis设置过期时间失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        memoryExpire.put(key, System.currentTimeMillis() + expireTime * 1000);
        return true;
    }
    
    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        if (isRedisAvailable()) {
            try {
                Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                return expire != null ? expire : -1;
            } catch (Exception e) {
                System.err.println("Redis获取过期时间失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        Long expireAt = memoryExpire.get(key);
        if (expireAt != null) {
            return Math.max(0, (expireAt - System.currentTimeMillis()) / 1000);
        }
        return -1;
    }
    
    /**
     * 模糊删除
     */
    public void deleteByPattern(String pattern) {
        if (isRedisAvailable()) {
            try {
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (!keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                }
                return;
            } catch (Exception e) {
                System.err.println("Redis模糊删除失败，切换到内存存储: " + e.getMessage());
                redisAvailable = false;
            }
        }
        // 降级到内存存储
        String prefix = pattern.replace("*", "");
        memoryStore.keySet().removeIf(key -> key.startsWith(prefix));
        memoryExpire.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }
    
    /**
     * 获取存储模式信息
     */
    public String getStorageMode() {
        return isRedisAvailable() ? "Redis" : "内存";
    }
    
    /**
     * 清理过期的内存数据
     */
    public void cleanupExpiredMemoryData() {
        if (!redisAvailable) {
            long now = System.currentTimeMillis();
            memoryExpire.entrySet().removeIf(entry -> {
                if (entry.getValue() <= now) {
                    memoryStore.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }
}