package com.babyfs.tk.dal.db;

import com.google.common.base.Preconditions;
import com.babyfs.tk.dal.db.annotation.Dao;

import javax.annotation.Nonnull;
import java.lang.reflect.Proxy;

/**
 * Dao的工厂,根据{@link Dao}等注解由于对应的Dao
 */
public class DaoFactory {
    private DaoSupport daoSupport;

    public DaoFactory(@Nonnull DaoSupport daoSupport) {
        Preconditions.checkArgument(daoSupport != null, "daoSupport");
        this.daoSupport = daoSupport;
    }

    /**
     * 根据{@IDao}的接口定义生成Dao的实现
     *
     * @param daoClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends IDao> T buildDao(@Nonnull Class<T> daoClass) {
        Preconditions.checkArgument(daoClass != null, "daoClass");
        Preconditions.checkArgument(daoClass.isInterface(), "The class " + daoClass + " must be an interface.");
        Dao dao = daoClass.getAnnotation(Dao.class);
        Preconditions.checkArgument(dao != null, "The class %s must be annotated with @Dao.",daoClass);
        Class[] interfaces = {daoClass};
        DaoInvocationHandler handler = new DaoInvocationHandler(daoClass, daoSupport, interfaces);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, handler);
    }

    /**
     * 暴露{@link DaoSupport}对象提供给外部直接调用
     * 目前主要是调用{@link DaoSupport#doTransaction(Class, java.util.Map, com.google.common.base.Function)}方法实现事务操作
     *
     * @return
     *
     */
    public DaoSupport getDaoSupport() {
        return daoSupport;
    }
}
