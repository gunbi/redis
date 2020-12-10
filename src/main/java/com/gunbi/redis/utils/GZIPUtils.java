package com.gunbi.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZIPUtils {

    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";

    public static byte[] compress(String str) {
        return compress(str, GZIP_ENCODE_UTF_8);
    }

    public static byte[] compress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        try {
            return compress(str.getBytes(encoding));
        } catch (Exception e) {
            log.error("gzip compress error.", e);
        }
        return null;
    }

    public static byte[] compress(byte[] data) throws Exception {
        GZIPOutputStream gout = null;
        ByteArrayOutputStream bout;
        try {
            bout = new ByteArrayOutputStream();
            gout = new GZIPOutputStream(bout);
            gout.write(data);
            gout.finish();
            return bout.toByteArray();
        } finally {
            try {
                if (gout != null) {
                    gout.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    public static byte[] decompress(byte[] data) throws Exception {
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(new ByteArrayInputStream(data));
            return IOUtils.toByteArray(gis);
        } finally {
            try {
                if (gis != null) {
                    gis.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

}
