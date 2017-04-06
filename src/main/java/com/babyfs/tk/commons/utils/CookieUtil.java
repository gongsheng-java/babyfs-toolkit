package com.babyfs.tk.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie相关工具类
 * <p/>
 */
public final class CookieUtil {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CookieUtil.class);

    private CookieUtil() {
    }

    /**
     * 设置Cookie(默认HttpOnly false)
     *
     * @param response
     * @param cookieName
     * @param value
     * @param time
     * @param domain
     */
    public static void setCookie(HttpServletResponse response, String cookieName, String value, int time, String domain) {
        setCookie(response, cookieName, value, time, domain, false);
    }

    /**
     * 设置Cookie(默认HttpOnly true)
     *
     * @param response
     * @param cookieName
     * @param value
     * @param time
     * @param domain
     */
    public static void setTokenCookie(HttpServletResponse response, String cookieName, String value, int time, String domain) {
        setCookie(response, cookieName, value, time, domain, true);
    }

    /**
     * 设置Cookie(设置ttpOnly)
     *
     * @param response
     * @param cookieName
     * @param value
     * @param time
     * @param domain
     * @param isHttpOnly
     */
    public static void setCookie(HttpServletResponse response, String cookieName, String value, int time, String domain, boolean isHttpOnly) {
        try {
            Cookie cookie = new Cookie(cookieName, value);
            if (domain != null) {
                cookie.setDomain(domain);
            }
            cookie.setPath("/");
            cookie.setMaxAge(time);
            cookie.setHttpOnly(isHttpOnly);
            response.addCookie(cookie);
        } catch (Exception e) {
            LOGGER.error("setCookie exception: " + e);
        }
    }

    /**
     * 移除Cookie
     *
     * @param response
     * @param cookieName
     */
    public static void removeCookie(HttpServletResponse response, String cookieName, String domain) {
        Cookie cookie = new Cookie(cookieName, "");
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 获取Cookie
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (int x1 = 0; x1 < cookies.length; x1++) {
            if (cookies[x1].getName().equals(cookieName)) {
                return cookies[x1].getValue();
            }
        }
        return null;
    }

}

