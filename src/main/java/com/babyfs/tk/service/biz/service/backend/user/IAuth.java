package com.babyfs.tk.service.biz.service.backend.user;

import com.babyfs.tk.service.biz.service.backend.user.model.bean.IAccountType;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.UserAccount;

/**
 * 验证服务
 */
public interface IAuth {
    /**
     * 校验用户名和密码是否匹配
     *
     * @param userAccount
     * @return
     */
    boolean auth(UserAccount userAccount);

    /**
     * 验证密码
     *
     * @param inputPassord 输入的密码
     * @param passowrd     hash后的密码
     * @param salt         盐值
     * @return true, 验证成功;false,验证失败
     */
    boolean auth(String inputPassord, String passowrd, String salt);

    /**
     * 根据用户名查找用户的信息
     *
     * @param userAccount
     * @return
     */
    UserAccount createToAddUserAccount(UserAccount userAccount);

    /**
     * 取得验证服务对应的账号类型
     *
     * @return
     */
    IAccountType getAccountType();

    /**
     * 是否支持修改密码
     *
     * @return true, 支持;false,不支持
     */
    boolean supportChangePassword();
}
