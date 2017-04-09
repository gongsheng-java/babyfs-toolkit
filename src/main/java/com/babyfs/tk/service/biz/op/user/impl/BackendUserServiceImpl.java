package com.babyfs.tk.service.biz.op.user.impl;

import com.babyfs.tk.service.biz.op.user.IAuth;
import com.babyfs.tk.service.biz.op.user.IBackendUserService;
import com.babyfs.tk.service.biz.op.user.Util;
import com.babyfs.tk.service.biz.op.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.op.user.model.UserAccount;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.utils.ListUtil;
import org.apache.commons.lang.StringUtils;
import com.babyfs.tk.service.biz.base.enums.Status;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 后台服务实现
 */
public class BackendUserServiceImpl implements IBackendUserService {
    @Inject
    Map<Integer, IAuth> authMap;

    @Inject
    IBackendUserDao backendUserDao;

    @Override
    public ServiceResponse<BackendUserEntity> auth(UserAccount userAccount) {
        Preconditions.checkNotNull(userAccount);
        Preconditions.checkNotNull(userAccount.getType());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getName()), "username");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getPassword()), "password");
        IAuth auth = authMap.get(userAccount.getType().getType());

        if (auth == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "Invalid account type");
        }

        String uniqName = Util.getInternalUserName(userAccount);
        List<BackendUserEntity> users = backendUserDao.getByName(uniqName);
        if (users == null || users.size() != 1) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "还未注册,请联系系统管理员");
        }
        if (!auth.auth(userAccount)) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "用户名或者密码错误");
        }

        return ServiceResponse.createSuccessResponse(users.get(0));
    }


    @Override
    public ServiceResponse<BackendUserEntity> addAccount(UserAccount userAccount) {
        Preconditions.checkNotNull(userAccount);
        Preconditions.checkNotNull(userAccount.getType());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getName()), "username");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getType().getSuffix()), "suffix");

        IAuth auth = authMap.get(userAccount.getType().getType());
        if (auth == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的验证服务");
        }

        UserAccount toAddAccount = auth.createToAddUserAccount(userAccount);
        if (toAddAccount == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无法创建用户");
        }

        String uniqName = Util.getInternalUserName(userAccount);
        List<BackendUserEntity> entities = backendUserDao.getByName(uniqName);
        if (entities != null && !entities.isEmpty()) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "用户已经添加过了");
        }

        BackendUserEntity userEntity = new BackendUserEntity();
        userEntity.setName(uniqName);
        userEntity.setPassword(toAddAccount.getPassword());
        userEntity.setSalt(toAddAccount.getSalt());
        userEntity.setDisplayName(toAddAccount.getDisplayName());
        userEntity.setEmail(toAddAccount.getEmail());
        userEntity.setPhone(toAddAccount.getPhone());
        userEntity.setType(userAccount.getType().getType());
        userEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        userEntity.setLastLoginTime(null);
        userEntity.setStat(toAddAccount.getStat().getValue());
        BackendUserEntity savedEntity = backendUserDao.save(userEntity);
        return ServiceResponse.createSuccessResponse(savedEntity);
    }

    @Override
    public ServiceResponse<Boolean> updateAccount(UserAccount userAccount) {
        Preconditions.checkNotNull(userAccount);
        Preconditions.checkNotNull(userAccount.getType());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getName()), "username");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccount.getType().getSuffix()), "suffix");
        IAuth auth = authMap.get(userAccount.getType().getType());
        if (auth == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的验证服务");
        }

        if (userAccount.getId() <= 0) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的用户参数");
        }

        BackendUserEntity userEntity = backendUserDao.get(userAccount.getId(), BackendUserEntity.class);
        if (userEntity == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的用户参数");
        }

        String uniqName = Util.getInternalUserName(userAccount);
        List<BackendUserEntity> entities = backendUserDao.getByName(uniqName);
        if (ListUtil.isNotEmtpy(entities)) {
            BackendUserEntity entityByName = entities.get(0);
            if (entityByName.getId() != userAccount.getId()) {
                return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "用户名已经存在");
            }
        }

        userEntity.setName(uniqName);
        userEntity.setDisplayName(userAccount.getDisplayName());
        userEntity.setEmail(userAccount.getEmail());
        userEntity.setPhone(userAccount.getPhone());
        userEntity.setType(userAccount.getType().getType());
        userEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        userEntity.setStat(userAccount.getStat().getValue());

        if (auth.supportChangePassword() && !Strings.isNullOrEmpty(userAccount.getPassword())) {
            //重新生成密码
            UserAccount passwordAccount = auth.createToAddUserAccount(userAccount);
            userEntity.setPassword(passwordAccount.getPassword());
            userEntity.setSalt(passwordAccount.getSalt());
        }

        boolean update = backendUserDao.update(userEntity);
        if (update) {
            return ServiceResponse.succResponse();
        } else {
            return ServiceResponse.failResponse();
        }
    }

    @Override
    public ServiceResponse<Boolean> changePassword(long id, String newPassword, String oldPassword) {
        if (id <= 0) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的用户参数");
        }

        newPassword = StringUtils.trimToNull(newPassword);
        if (Strings.isNullOrEmpty(newPassword)) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的密码");
        }

        BackendUserEntity userEntity = backendUserDao.get(id, BackendUserEntity.class);
        if (userEntity == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的用户参数");
        }

        IAuth auth = this.authMap.get(userEntity.getType());
        if (auth == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "不支持的认证类型");
        }

        if (!auth.supportChangePassword()) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "不支持修改密码");
        }

        if (!auth.auth(oldPassword, userEntity.getPassword(), userEntity.getSalt())) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "密码验证失败");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setPassword(newPassword);
        UserAccount passwordAccount = auth.createToAddUserAccount(userAccount);

        boolean ret = backendUserDao.updatePassword(id, passwordAccount.getPassword(), passwordAccount.getSalt()) > 0;
        return new ServiceResponse<>(ret, 0, ret, null);
    }

    @Override
    public ServiceResponse<Boolean> updateAccountStatus(long id, Status status) {
        Preconditions.checkArgument(id > 0);
        boolean ret = backendUserDao.updateStatus(id, status.getValue()) > 0;
        return new ServiceResponse<>(ret, 0, ret, null);
    }

    @Override
    public ServiceResponse<BackendUserEntity> queryUserById(long id) {
        Preconditions.checkArgument(id > 0);
        return ServiceResponse.createSuccessResponse(backendUserDao.get(id, BackendUserEntity.class));
    }

    @Override
    public ServiceResponse<BackendUserEntity> getByDisplayName(String displayName) {
        Preconditions.checkNotNull(displayName);

        List<BackendUserEntity> userEntityList = this.backendUserDao.getByDisplayName(displayName);
        if (ListUtil.isEmpty(userEntityList)) {
            return ServiceResponse.createFailResponse("用户不存在");
        }

        return ServiceResponse.createSuccessResponse(userEntityList.get(0));
    }
}
