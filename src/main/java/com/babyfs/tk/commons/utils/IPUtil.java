package com.babyfs.tk.commons.utils;

import com.google.common.base.Strings;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理IP相关的工具类
 * <p/>
 */
public final class IPUtil {

    private IPUtil() {
    }

    /**
     * 取得当前用户IP
     *
     * @param request
     * @return
     */
    public static String getIP(HttpServletRequest request) {
        // 根据优先级规则，如果取得一个有效ip，立即返回
        String ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("x-forwarded-for");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getRemoteAddr();
        return ip;
    }

    /**
     * 是否是有效的ip地址
     *
     * @param ip
     * @return
     */
    private static boolean isValidIp(String ip) {
        return (!Strings.isNullOrEmpty(ip)) && (!"unknown".equalsIgnoreCase(ip));
    }

    /**
     * 取得客户端IP
     *
     * @param request
     * @return
     */
    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (isValidIp(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (isValidIp(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (isValidIp(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (isValidIp(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (isValidIp(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


}
