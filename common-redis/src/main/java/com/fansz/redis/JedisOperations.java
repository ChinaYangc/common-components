package com.fansz.redis;

import com.fansz.redis.support.JedisCallback;
import redis.clients.jedis.Jedis;

public interface JedisOperations {

    Jedis getRedisClient();

    void returnResource(Jedis jedis);

    <T> T execute(JedisCallback<T> redisCallback);

}
