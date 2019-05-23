package com.babyfs.tk.trace;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Objects;

/**
 * Create by gao.wei on 2019-03-24
 */
public class TraceGenerator {
 
    public static final int INIT_COUNT = 24;
 
    public static final int APPEND_COUNT = 8;
 
    public static final String SEPARATOR = ".";
 
    public static final int MAX_CHAR_COUNT = 128;
 
    public static String generateTrace() {
        return RandomStringUtils.randomAlphanumeric(INIT_COUNT);
    }
 
    public static String appendTrace(String origin) {
        if (Objects.isNull(origin)){
            origin = "";
        }
        //如果超长,不再追加(没有考虑分隔符)
        if (origin.length() + APPEND_COUNT > MAX_CHAR_COUNT){
            return origin;
        }
        return origin.concat(SEPARATOR).concat(RandomStringUtils.randomAlphanumeric(APPEND_COUNT));
    }
}