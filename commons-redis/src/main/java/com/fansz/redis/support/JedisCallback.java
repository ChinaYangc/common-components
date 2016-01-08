package com.fansz.redis.support;

import redis.clients.jedis.Jedis;

/**
 * Created by allan on 15/12/16.
 */
public interface JedisCallback<T> {

    /**
     * @param jedis active Redis connection
     * @return a result object or {@code null} if none
     * @throws Exception
     */
    T doInRedis(Jedis jedis) throws Exception;
}
