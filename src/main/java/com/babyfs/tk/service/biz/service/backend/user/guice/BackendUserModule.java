package com.babyfs.tk.service.biz.service.backend.user.guice;

import com.google.inject.multibindings.MapBinder;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.dal.guice.DalShardModule;
import com.babyfs.tk.service.biz.service.backend.user.IAuth;
import com.babyfs.tk.service.biz.service.backend.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.service.backend.user.dal.IBackendUserRoleDao;
import com.babyfs.tk.service.biz.service.backend.user.dal.IRoleDao;
import com.babyfs.tk.service.biz.service.backend.user.dal.IRolePermissionDao;
import com.babyfs.tk.service.biz.service.backend.user.internal.InternalAuthImpl;
import com.babyfs.tk.service.biz.service.backend.user.internal.LDAPAuthImpl;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.AccountType;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.BackendUserEntity;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.BackendUserRoleEntity;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.RoleEntity;
import com.babyfs.tk.service.biz.service.backend.user.model.entity.RolePermissionEntity;

/**
 * 后台用户服务的Module,包括用户验证,角色管理和权限
 */
public class BackendUserModule extends ServiceModule {

    @Override
    protected void configure() {
        DalShardModule.bindEntityAndDao(this.binder(), BackendUserEntity.class, IBackendUserDao.class);
        DalShardModule.bindEntityAndDao(this.binder(), RoleEntity.class, IRoleDao.class);
        DalShardModule.bindEntityAndDao(this.binder(), RolePermissionEntity.class, IRolePermissionDao.class);
        DalShardModule.bindEntityAndDao(this.binder(), BackendUserRoleEntity.class, IBackendUserRoleDao.class);

        //Auth
        MapBinder<Integer, IAuth> authMapBinder = MapBinder.newMapBinder(binder(), Integer.class, IAuth.class);
        authMapBinder.addBinding(AccountType.LDAP.getType()).to(LDAPAuthImpl.class).asEagerSingleton();
        authMapBinder.addBinding(AccountType.INTERNAL.getType()).to(InternalAuthImpl.class).asEagerSingleton();
    }
}
