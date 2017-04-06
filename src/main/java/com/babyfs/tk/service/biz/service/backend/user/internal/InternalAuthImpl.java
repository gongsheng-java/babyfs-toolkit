package com.babyfs.tk.service.biz.service.backend.user.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.security.PasswordSaltHash;
import com.babyfs.tk.service.basic.security.PasswordSaltMD5Hash;
import com.babyfs.tk.service.biz.service.backend.user.IAuth;
import com.babyfs.tk.service.biz.service.backend.user.Util;
import com.babyfs.tk.service.biz.service.backend.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.AccountType;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.IAccountType;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.UserAccount;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.BackendUserEntity;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * 内部用户认证
 */
public class InternalAuthImpl implements IAuth {
    @Inject
    IBackendUserDao backendUserDao;

    private final PasswordSaltHash passwordSaltMD5Hash = new PasswordSaltMD5Hash(5);

    @Override
    public boolean auth(UserAccount userAccount) {
        Preconditions.checkNotNull(userAccount);
        Preconditions.checkArgument(userAccount.getType() == getAccountType(), "Invalid account type");
        if (Strings.isNullOrEmpty(userAccount.getName()) || Strings.isNullOrEmpty(userAccount.getPassword())) {
            return false;
        }

        String uniqName = Util.getInternalUserName(userAccount);
        List<BackendUserEntity> userEntities = backendUserDao.getByName(uniqName);
        if (userEntities == null || userEntities.size() != 1) {
            return false;
        }
        BackendUserEntity userEntity = userEntities.get(0);
        return passwordSaltMD5Hash.validatePassword(userAccount.getPassword(), userEntity.getPassword(), userEntity.getSalt());
    }

    /**
     * 验证密码
     *
     * @param inputPassord 输入的密码
     * @param passowrd     hash后的密码
     * @param salt         盐值
     * @return
     */
    @Override
    public boolean auth(String inputPassord, String passowrd, String salt) {
        if (Strings.isNullOrEmpty(inputPassord) || Strings.isNullOrEmpty(passowrd) || Strings.isNullOrEmpty(salt)) {
            return false;
        }
        return passwordSaltMD5Hash.validatePassword(inputPassord, passowrd, salt);
    }

    @Override
    public UserAccount createToAddUserAccount(UserAccount userAccount) {
        UserAccount toAddUserAccount = new UserAccount();
        BeanUtils.copyProperties(userAccount, toAddUserAccount);
        Pair<String, String> passwordSalt = passwordSaltMD5Hash.createHash(userAccount.getPassword());
        toAddUserAccount.setPassword(passwordSalt.first);
        toAddUserAccount.setSalt(passwordSalt.second);
        return toAddUserAccount;
    }

    @Override
    public IAccountType getAccountType() {
        return AccountType.INTERNAL;
    }

    @Override
    public boolean supportChangePassword() {
        return true;
    }
}
