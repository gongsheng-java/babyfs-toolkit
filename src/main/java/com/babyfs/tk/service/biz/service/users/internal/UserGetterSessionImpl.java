package com.babyfs.tk.service.biz.service.users.internal;

import com.google.common.base.Preconditions;
import com.babyfs.tk.service.biz.service.users.IUserGetter;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 从Session中取得请求的用户
 */
public class UserGetterSessionImpl<T> implements IUserGetter<T> {
    /**
     * {@link HttpSession}中存储用户的属性名称
     */
    private final String sessionUserAttrName;

    /**
     * @param sessionUserAttrName 在seession中保存用户的属性名称
     */
    public UserGetterSessionImpl(String sessionUserAttrName) {
        this.sessionUserAttrName = Preconditions.checkNotNull(StringUtils.trimToNull(sessionUserAttrName), "The sessionUserAttrName不能为空");
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRequestUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (T) session.getAttribute(this.sessionUserAttrName);
    }
}
