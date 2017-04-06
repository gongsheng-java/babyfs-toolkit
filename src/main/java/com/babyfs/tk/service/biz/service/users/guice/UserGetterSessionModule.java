package com.babyfs.tk.service.biz.service.users.guice;

import com.google.common.base.Strings;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.service.users.IUserGetter;
import com.babyfs.tk.service.biz.service.users.internal.UserGetterSessionImpl;

/**
 * 使用{@link javax.servlet.http.HttpSession}实现的{@link com.babyfs.tk.service.biz.service.users.IUserGetter}
 */
public class UserGetterSessionModule extends ServiceModule {
    private final String sessionUserAttrName;
    private final String userGetterInstanceName;

    /**
     * @param sessionUserAttrName    在session中保存用户的属性名,非空
     * @param userGetterInstanceName {@link com.babyfs.tk.service.biz.service.users.IUserGetter}的实例名称,可以为空
     */
    public UserGetterSessionModule(String sessionUserAttrName, String userGetterInstanceName) {
        this.sessionUserAttrName = sessionUserAttrName;
        this.userGetterInstanceName = userGetterInstanceName;
    }

    @Override
    protected void configure() {
        if (Strings.isNullOrEmpty(this.userGetterInstanceName)) {
            bindService(IUserGetter.class, new UserGetterSessionImpl(this.sessionUserAttrName));
        } else {
            bindService(IUserGetter.class, new UserGetterSessionImpl(this.sessionUserAttrName), this.userGetterInstanceName);
        }
    }
}
