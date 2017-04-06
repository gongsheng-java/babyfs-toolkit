package com.babyfs.tk.service.biz.service.backend.user.internal;

import com.google.common.collect.Lists;
import com.babyfs.tk.service.biz.service.backend.user.Util;
import com.babyfs.tk.service.biz.service.backend.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.AccountType;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.UserAccount;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.BackendUserEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

/**
 *
 */
public class InternalAuthImplTest {

    @Test
    public void testCreateToAddUserAccount() throws Exception {
        IBackendUserDao backendUserDao = Mockito.mock(IBackendUserDao.class);

        InternalAuthImpl internalAuth = new InternalAuthImpl();
        internalAuth.backendUserDao = backendUserDao;

        UserAccount userAccount = new UserAccount();
        userAccount.setType(AccountType.INTERNAL);
        userAccount.setName("w");
        userAccount.setPassword("1");
        UserAccount toAddUserAccount = internalAuth.createToAddUserAccount(userAccount);
        System.out.println(toAddUserAccount.getPassword());
        System.out.println(toAddUserAccount.getSalt());

        BackendUserEntity entity = new BackendUserEntity();
        entity.setPassword(toAddUserAccount.getPassword());
        entity.setSalt(toAddUserAccount.getSalt());
        when(backendUserDao.getByName(Util.getInternalUserName(userAccount))).thenReturn(Lists.newArrayList(entity));

        userAccount.setSalt(toAddUserAccount.getSalt());
        Assert.assertTrue(internalAuth.auth(userAccount));
    }
}