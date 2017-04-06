package com.babyfs.tk.service.biz.service.users;

import javax.servlet.http.HttpServletRequest;

/**
 * 从Http请求中取得当前用户
 */
public interface IUserGetter<T> {
    /**
     * 取得当前请求的用户
     *
     * @param request
     * @return
     */
    T getRequestUser(HttpServletRequest request);
}
