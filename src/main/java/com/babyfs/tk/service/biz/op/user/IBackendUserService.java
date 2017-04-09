package com.babyfs.tk.service.biz.op.user;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.biz.op.user.model.UserAccount;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;
import com.babyfs.tk.service.biz.base.enums.Status;

/**
 * 后台用户服务
 */
public interface IBackendUserService {
    /**
     * 认证用户,如果验证通过,返回对应的用户,否则返回认证失败
     *
     * @param userAccount
     * @return
     */
    ServiceResponse<BackendUserEntity> auth(UserAccount userAccount);

    /**
     * 添加一个账号
     *
     * @param userAccount user account
     * @return 操作结果
     */
    ServiceResponse<BackendUserEntity> addAccount(UserAccount userAccount);


    /**
     * 更新一个账号,如果userAccount的密码为空,只更新基本信息;如果userAccount密码不为空,则同时更新密码和基本信息
     *
     * @param userAccount use account
     * @return true, 更新成功;false,更新失败
     */
    ServiceResponse<Boolean> updateAccount(UserAccount userAccount);

    /**
     * 修改账号密码
     *
     * @param id          账号id
     * @param newPassword 新密码
     * @param oldPassword 旧密码
     * @return
     */
    ServiceResponse<Boolean> changePassword(long id, String newPassword, String oldPassword);

    /**
     * 更新账号的状态
     *
     * @param id
     * @param status
     * @return
     */
    ServiceResponse<Boolean> updateAccountStatus(long id, Status status);

    /**
     * 获取后台用户
     *
     * @param id
     * @return
     */
    ServiceResponse<BackendUserEntity> queryUserById(long id);

    /**
     * 根据显示名称获取后台用户实体
     *
     * @param displayName 显示名称
     * @return 后台用户实体
     */
    ServiceResponse<BackendUserEntity> getByDisplayName(String displayName);
}
