package com.gunbi.redis.client;


import redis.clients.jedis.ScanResult;

/**
 * Created on 2020/12/10.
 *
 * @author hanzhong
 */
public interface RedisClient {
    String set(String key,String value);

    String get(String key);

    Integer del(String key);

    ScanResult scan(Integer cursor, String match, Integer count);
}
