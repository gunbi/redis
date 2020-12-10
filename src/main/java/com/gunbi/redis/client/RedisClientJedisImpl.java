package com.gunbi.redis.client;

import com.google.common.collect.Maps;
import com.gunbi.redis.utils.GZIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2020/11/10.
 *
 * @author hanzhong
 */
@Slf4j
public class RedisClientJedisImpl {

    private static final int REDIS_POOL_MAX_ACTIVE = 10;
    private static final int REDIS_POOL_MAX_WAIT = 500;
    private static final int REDIS_POOL_MAX_IDLE = 5;
    private static final int REDIS_POOL_MIN_IDLE = 1;
    private static final int LOCK_TIME_OUT = 10 * 6000;
    private static final String LOCK_SUCCESS = "OK";
    private static JedisPool jedisPool = null;

    public RedisClientJedisImpl(String host, int port, int timeout) {
        jedisPool = new JedisPool(poolConfig(), host, port, timeout);
    }

    private static JedisPoolConfig poolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(REDIS_POOL_MAX_IDLE);
        poolConfig.setMaxTotal(REDIS_POOL_MAX_ACTIVE);
        poolConfig.setMaxWaitMillis(REDIS_POOL_MAX_WAIT);
        poolConfig.setMinIdle(REDIS_POOL_MIN_IDLE);
        return poolConfig;
    }

    public void set(String key, String value) {
        this.consumer(jedis -> jedis.set(key, value));
    }

    public void del(String key) {
        this.consumer(jedis -> jedis.del(key));
    }

    public void set(String key, String value, int errorInvalidSeconds) {
        this.consumer(jedis -> {
            SetParams setParams = new SetParams().nx().ex(errorInvalidSeconds);
            jedis.set(key, value, setParams);
        });
    }

    public String get(String key) {
        return this.execute(jedis -> jedis.get(key));
    }

    public void compressAndHSet(String cacheKey, String key, String values) throws Exception {
        if (StringUtils.isBlank(values)) {
            return;
        }
        byte[] compress = GZIPUtils.compress(values.getBytes());
        this.consumer(jedis -> jedis.hset(cacheKey.getBytes(), key.getBytes(), compress));
    }

    public Map<String, String> decompressAndHMGet(String key) {
        return this.execute(jedis -> {
            Map<String, String> result = Maps.newHashMap();
            Map<byte[], byte[]> compressedMap = jedis.hgetAll(key.getBytes());
            for (Map.Entry<byte[], byte[]> entry : compressedMap.entrySet()) {
                try {
                    result.put(new String(entry.getKey()), new String(GZIPUtils.decompress(entry.getValue())));
                } catch (Exception e) {
                    log.error("decompress error, e", e);
                }
            }
            return result;
        });
    }

    public boolean lock(String lockName, String identifier) {
        return this.execute(jedis -> {
            //NX：不存在则Set
            String result = jedis.set(lockName, identifier, SetParams.setParams().nx().px(LOCK_TIME_OUT));
            return result != null && result.equals(LOCK_SUCCESS);
        });
    }

    public boolean unLock(String lockName, String identifier) {
        return this.execute(jedis -> {
            if (jedis.get(lockName).equals(identifier)) {
                //可能存在： 删除之前刚好过期，此时删除的就是其他线程刚创建的锁。最好是用一个lua脚本来实现 get and del的原子性。
                return jedis.del(lockName) > 0;
            }
            return false;
        });
    }

    private <T> T execute(Function<Jedis, T> executor) {
        Jedis jedis = jedisPool.getResource();
        T result = null;
        try {
            result = executor.apply(jedis);
        } catch (Exception e) {
            log.error("executor jedis failed, error: ", e);
        } finally {
            close(jedis);
        }
        return result;
    }

    private void consumer(Consumer<Jedis> consumer) {
        Jedis jedis = jedisPool.getResource();
        try {
            consumer.accept(jedis);
        } catch (Exception e) {
            log.error("consumer jedis failed, error: ", e);
        } finally {
            close(jedis);
        }
    }

    private void close(Jedis jedisClient) {
        if (jedisClient != null) {
            jedisClient.close();
        }
    }
}
