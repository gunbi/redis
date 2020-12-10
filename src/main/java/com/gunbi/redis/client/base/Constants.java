package com.gunbi.redis.client.base;

/**
 * Created on 2020/12/10.
 *
 * @author hanzhong
 */
public class Constants {
    /**
     * redis client pool config
     */
    public static final int REDIS_POOL_MAX_ACTIVE = 10;
    public static final int REDIS_POOL_MAX_WAIT = 500;
    public static final int REDIS_POOL_MAX_IDLE = 5;
    public static final int REDIS_POOL_MIN_IDLE = 1;

    /**
     * redis client lock config
     */
    public static final int LOCK_TIME_OUT = 10 * 6000;


    public static final String OPERATION_SUCCESS = "OK";
}
