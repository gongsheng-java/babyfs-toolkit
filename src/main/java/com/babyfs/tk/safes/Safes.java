package com.babyfs.tk.safes;

import java.util.*;

/**
 * safes.....
 *
 * Create by gao.wei on 2019-04-22
 */
public class Safes {

    public static Boolean of(Boolean source) {
        if (Objects.isNull(source)) {
            return false;
        }
        return source;
    }

    public static Byte of(Byte source) {
        if (Objects.isNull(source)) {
            return 0;
        }
        return source;
    }

    public static Short of(Short source) {
        if (Objects.isNull(source)) {
            return 0;
        }
        return source;
    }

    public static Integer of(Integer source) {
        if (Objects.isNull(source)) {
            return 0;
        }
        return source;
    }

    public static Long of(Long source) {
        if (Objects.isNull(source)) {
            return 0L;
        }
        return source;
    }

    public static String of(String source) {
        if (Objects.isNull(source)) {
            return "";
        }
        return source;
    }

    public static <T> List<T> of(List<T> source) {
        if (Objects.isNull(source)) {
            return new ArrayList<>();
        }
        return source;
    }

    public static <T> Set<T> of(Set<T> source) {
        if (Objects.isNull(source)) {
            return new HashSet<>();
        }
        return source;
    }

    public static <K,V> Map<K,V> of(Map<K,V> source) {
        if (Objects.isNull(source)) {
            return new HashMap<>();
        }
        return source;
    }

    public static <T> Iterator<T> of(Iterator<T> iterable) {
        if (Objects.isNull(iterable)) {
            return Collections.emptyIterator();
        }
        return iterable;
    }
    public static <T> Iterable<T> of(Iterable<T> iterable) {
        if (Objects.isNull(iterable)) {
            return () -> Collections.emptyIterator();
        }
        return iterable;
    }
}
