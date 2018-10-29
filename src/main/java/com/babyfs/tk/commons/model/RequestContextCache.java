package com.babyfs.tk.commons.model;

import com.alibaba.dubbo.cache.support.threadlocal.ThreadLocalCache;
import com.babyfs.tk.rpc.Request;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @program BabyFs
 * @description: 上下文缓存
 * @author: huyihuan
 * @create: 2018/10/22
 */
@Component
public class RequestContextCache {
    private static ThreadLocal<Map<String, Object>> cacheContext;

    static {
        cacheContext = new ThreadLocal() {
            @Override
            protected Map<String, Object> initialValue() {
                return new HashMap<>();
            }
        };
    }

    public static Object get(String key) {
        return cacheContext.get().get(key);
    }

    public static void set(String key, Object val) {
        cacheContext.get().put(key, val);
    }

    public static void clear() {
        cacheContext.remove();
    }
}
