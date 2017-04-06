package com.babyfs.tk.dal.orm;

import java.lang.reflect.Method;

/**
 */
public final class Util {
    private Util(){

    }
    /**
     * @param str
     * @return
     */
    public static String toUpper(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * @param str
     * @return
     */
    public static String toLower(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * @param name
     * @param clazz
     * @return
     */
    public static Method findMethodByName(String name, Class clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }



}
