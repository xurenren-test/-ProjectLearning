package org.tinygame.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis实用工具类
 */
public final class RedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    // redis连接池
    private static JedisPool jedisPool = null;

    private RedisUtil() {
    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            jedisPool = new JedisPool("r-tj7yces4z309ti56qy.redis.rds.aliyuncs.com/redisConfig/vpc/BAOYUN/test/iyb-user", 6379);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取redis实例
     * @return
     */
    public static Jedis getRedis() {
        if (jedisPool == null) {
            throw new RuntimeException("jedisPool 尚未初始化");
        }

        Jedis redis = jedisPool.getResource();
        // redis密码验证
        redis.auth("HbKDRR1%HbMETVsC");
        return redis;
    }
}
