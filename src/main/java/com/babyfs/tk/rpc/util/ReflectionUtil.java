package com.babyfs.tk.rpc.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Method;

/**
 * Java反射工具类
 */
public final class ReflectionUtil {
    private ReflectionUtil() {

    }

    /**
     * 检查两个方法的签名是否一致
     *
     * @param m1
     * @param m2
     * @return
     */
    public static boolean sigEquals(Method m1, Method m2) {
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        if (!m1.getReturnType().equals(m2.getReturnType())) {
            return false;
        }
        Class<?>[] parameterTypes1 = m1.getParameterTypes();
        Class<?>[] parameterTypes2 = m2.getParameterTypes();
        if (parameterTypes1.length != parameterTypes2.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes1.length; i++) {
            if (!parameterTypes1[i].equals(parameterTypes2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 取得方法的签名标识
     *
     * @param m1
     * @return md5(m1.returnType+paramterTypes)
     */
    public static String methodSignature(Method m1) {
        StringBuilder sb = new StringBuilder();
        sb.append(m1.getReturnType().getName());
        Class<?>[] parameterTypes = m1.getParameterTypes();
        for (Class parameterType : parameterTypes) {
            sb.append(parameterType.getName());
        }
        return DigestUtils.md5Hex(sb.toString());
    }
}
