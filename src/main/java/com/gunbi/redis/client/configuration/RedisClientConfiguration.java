package com.gunbi.redis.client.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 2020/12/10.
 *
 * @author hanzhong
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisClientConfiguration {
    private String host;
    private int port;
    private int timeout;
}
