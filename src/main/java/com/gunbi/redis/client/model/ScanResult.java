package com.gunbi.redis.client.model;

import lombok.Data;

import java.util.List;

/**
 * Created on 2020/12/10.
 *
 * @author hanzhong
 */
@Data
public class ScanResult {
    private String cursor;
    private List<String> result;
}
