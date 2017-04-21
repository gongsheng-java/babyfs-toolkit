package com.babyfs.tk.service.biz.op.user.guice;

import com.babyfs.tk.service.biz.op.user.dal.IRoleDao;
import com.babyfs.tk.service.biz.op.user.model.AccountType;
import com.babyfs.tk.service.biz.op.user.model.entity.RoleEntity;
import com.google.inject.multibindings.MapBinder;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.dal.guice.DalShardModule;
import com.babyfs.tk.service.biz.op.user.IAuth;
import com.babyfs.tk.service.biz.op.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.op.user.dal.IBackendUserRoleDao;
import com.babyfs.tk.service.biz.op.user.dal.IRolePermissionDao;
import com.babyfs.tk.service.biz.op.user.impl.InternalAuthImpl;
import com.babyfs.tk.service.biz.op.user.impl.LDAPAuthImpl;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserRoleEntity;
import com.babyfs.tk.service.biz.op.user.model.entity.RolePermissionEntity;

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
        authMapBinder.addBinding(AccountType.INTERNAL.getType()).to(InternalAuthImpl.class).asEagerSingleton();
    }
}
