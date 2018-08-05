package com.babyfs.tk.commons.utils;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Map Util
 */
public final class MapUtil {
    private MapUtil() {

    }

    /**
     * 从map中取得key，如果key对应的value为null，则返回defaultValue
     *
     * @param map
     * @param key
     * @param defaultValue
     * @return
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
        V value = map.get(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * 如果map中不包含key,则向其中增加key和value
     *
     * @param map
     * @param key
     * @param value
     * @param <K>
     * @param <V>
     */
    public static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    /**
     * 如果map中不包含key,则向其中增加key和value
     *
     * @param map
     * @param key
     * @param valueFn
     * @param <K>
     * @param <V>
     */
    public static <K, V> void putIfAbsent(Map<K, V> map, K key, Function<Void, V> valueFn) {
        if (!map.containsKey(key)) {
            map.put(key, valueFn.apply(null));
        }
    }

    /**
     * 从map中取得整数,如果map为空或者key不存在,返回默认值defaultValue
     *
     * @param map
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static <K, V> int getInt(Map<K, V> map, K key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        Object val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val instanceof String) {
            String s = (String) val;
            if (Strings.isNullOrEmpty(s)) {
                return defaultValue;
            } else {
                return Integer.parseInt(s);
            }
        } else {
            throw new NumberFormatException("Not a int " + val);
        }
    }

    /**
     * 从map中取得整数,如果map为空或者key不存在,返回默认值defaultValue
     *
     * @param map
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static <K, V> long getLong(Map<K, V> map, K key, long defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        Object val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            String s = (String) val;
            if (Strings.isNullOrEmpty(s)) {
                return defaultValue;
            } else {
                return Long.parseLong(s);
            }
        } else {
            throw new NumberFormatException("Not a int " + val);
        }
    }

    /**
     * 从map中取得字符串,如果map为空或者key不存在,返回默认值defaultValue
     *
     * @param map
     * @param key
     * @param defaultValue
     * @return
     * @throws RuntimeException
     */
    public static <K, V> String getString(Map<K, V> map, K key, String defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        Object val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof String) {
            return (String) val;
        } else {
            throw new RuntimeException("Not a string " + val);
        }
    }


    /**
     * 转换map
     *
     * @param map
     * @param transformFunc
     * @param <K1>
     * @param <V1>
     * @param <K2>
     * @param <V2>
     * @return
     */
    public static <K1, V1, K2, V2> Map<K2, V2> transformMap(Map<K1, V1> map, Function<Pair<K1, V1>, Pair<K2, V2>> transformFunc) {
        Map<K2, V2> ret = Maps.newHashMapWithExpectedSize(map.size());
        for (Map.Entry<K1, V1> entry : map.entrySet()) {
            Pair<K2, V2> transformed = transformFunc.apply(Pair.of(entry.getKey(), entry.getValue()));
            if (transformed != null) {
                ret.put(transformed.first, transformed.second);
            }
        }
        return ret;
    }

    /**
     * 是否为空字典
     *
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * 是否不是空字典
     *
     * @param map
     * @return
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 实体转map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> object2Map(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
