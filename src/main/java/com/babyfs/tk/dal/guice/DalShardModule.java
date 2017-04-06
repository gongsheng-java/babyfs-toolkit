package com.babyfs.tk.dal.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.*;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.shard.*;
import com.babyfs.tk.dal.db.shard.impl.TomcatDataSourceCreator;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.Set;

/**
 */
public class DalShardModule extends PrivateModule {
    /**
     * 默认数据源称的Guice注册名称
     */
    static final String NAME_DEFAULT_DATASOURCE = "DAL.DEFAULT.DATASOURCE";
    /**
     * 默认Shard组的Guice注册名称
     */
    static final String NAME_DEFAULT_SHARD_GROUP = "DAL.DEFAULT.SHARD.GROUP";
    /**
     * 默认shard id 的guice注册名称
     */
    static final String NAME_DEFAULT_SHARD_ID = "DAL.DEFAULT.SHARD.ID";
    /**
     * 数据库实例{@link DBInstance}集合的Guice注册名称
     */
    static final String NAME_DB_INSTANCE_SET = "DAL.DB.INSTANCE.SET";
    /**
     * 数据库shard实例 {@link DBShardInstance}集合的Guice注册名称
     */
    static final String NAME_DB_SHARD_INSTANCE_SET = "DAL.DB.SHARD.INSTANCE.SET";
    /**
     * entity shard{@link EntityShard}集合的Guice注册名称
     */
    static final String NAME_DB_ENTITY_SHARD_SET = "DAL.DB.ENTITY.SHARD.INSTANCE.SET";
    /**
     * 实体类型的名称
     */
    static final String NAME_ENTITY_CLASS_SET = "DAL.ENTITY.CLASS.SET";

    /**
     * 数据库实例{@link DBInstance}集合在Guice中注册的Key
     */
    public static final Key<Set<DBInstance>> DB_INSTANCE_SET_KEY = getSetKey(DBInstance.class, NAME_DB_INSTANCE_SET);
    /**
     * 数据库shard实例{@link DBShardInstance}集合在Guice中注册的Key
     */
    public static final Key<Set<DBShardInstance>> DB_SHARD_INSTANCE_SET_KEY = getSetKey(DBShardInstance.class, NAME_DB_SHARD_INSTANCE_SET);
    /**
     * 数据库实体{@link EntityShard}集合在Guice中注册的Key
     */
    public static final Key<Set<EntityShard>> ENTITY_SHARD_SET_KEY = getSetKey(EntityShard.class, NAME_DB_ENTITY_SHARD_SET);


    @Override
    protected void configure() {
        //实体类型
        bind(EntityMetaSet.class).toProvider(DalModuleProviders.EntityMetaSetProvider.class).asEagerSingleton();

        //数据源
        bind(IDataSourceCreator.class).to(TomcatDataSourceCreator.class).asEagerSingleton();
        bind(ShardDataSourceContainer.class).toProvider(DalModuleProviders.ShardDataSourceContainerProvier.class).asEagerSingleton();
        bind(ShardDataSource.class).toProvider(DalModuleProviders.ShardDataSourceProvider.class).asEagerSingleton();

        //DaoSupport
        bind(DaoSupport.class).toProvider(DalModuleProviders.DaoSupportProvider.class).asEagerSingleton();

        //DaoFactory
        Key<DaoFactory> daoFactoryKey = Key.get(DaoFactory.class);
        bind(daoFactoryKey).toProvider(DalModuleProviders.DaoFactoryProvider.class).asEagerSingleton();
        expose(daoFactoryKey);
    }

    /**
     * 创建一个用于注册{@link DBInstance}的Mutilbinder
     *
     * @param binder
     * @return
     */
    public static Multibinder<DBInstance> createDBInstanceMutilbinder(@Nonnull Binder binder) {
        Preconditions.checkNotNull(binder, "binder");
        return Multibinder.newSetBinder(binder, DBInstance.class, Names.named(DalShardModule.NAME_DB_INSTANCE_SET));
    }

    /**
     * 创建一个用于注册{@link DBShardInstance}的Mutilbinder
     *
     * @param binder
     * @return
     */
    public static Multibinder<DBShardInstance> createDBShardInstanceMutilbinder(@Nonnull Binder binder) {
        Preconditions.checkNotNull(binder, "binder");
        return Multibinder.newSetBinder(binder, DBShardInstance.class, Names.named(DalShardModule.NAME_DB_SHARD_INSTANCE_SET));
    }

    /**
     * 创建一个用于注册{@link EntityShard}的Mutilbinder
     *
     * @param binder
     * @return
     */
    public static Multibinder<EntityShard> createEntityShardMutilbinder(@Nonnull Binder binder) {
        Preconditions.checkNotNull(binder, "binder");
        return Multibinder.newSetBinder(binder, EntityShard.class, Names.named(DalShardModule.NAME_DB_ENTITY_SHARD_SET));
    }

    /**
     * 创建一个用于注册{@link IEntity}类的Multibinder
     *
     * @param binder
     * @return
     */
    public static Multibinder<Class> createEntityClassMutilbinder(@Nonnull Binder binder) {
        Preconditions.checkNotNull(binder, "binder");
        return Multibinder.newSetBinder(binder, Class.class, Names.named(DalShardModule.NAME_ENTITY_CLASS_SET));
    }

    /**
     * 绑定默认的数据源
     *
     * @param binder
     * @param dataSource
     */
    public static void bindDefaultDataSource(@Nonnull Binder binder, @Nullable DataSource dataSource) {
        if (dataSource != null) {
            binder.bind(DataSource.class).annotatedWith(Names.named(NAME_DEFAULT_DATASOURCE)).toInstance(dataSource);
        }
    }


    /**
     * 绑定默认的shard信息
     *
     * @param binder
     * @param defaultShardGroup
     * @param defaultShardId
     */
    public static void bindDefaultShard(@Nonnull Binder binder, @Nonnull String defaultShardGroup, @Nonnull String defaultShardId) {
        Preconditions.checkArgument(binder != null, "binder");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultShardGroup), "defaultShardGroup");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultShardId), "defaultShardId");
        binder.bindConstant().annotatedWith(Names.named(NAME_DEFAULT_SHARD_GROUP)).to(defaultShardGroup);
        binder.bindConstant().annotatedWith(Names.named(NAME_DEFAULT_SHARD_ID)).to(defaultShardId);
    }

    /**
     * 绑定dao的工具类
     *
     * @param binder
     * @param daoClass
     */
    public static void bindDao(@Nonnull Binder binder, @Nonnull Class daoClass) {
        Preconditions.checkArgument(daoClass.isInterface(), "daoClass %s must be an interface class.", daoClass);
        Preconditions.checkArgument(IDao.class.isAssignableFrom(daoClass), "daoClass %s must be a sub interface of %s.", daoClass);
        DalModuleProviders.DaoProvider provider = new DalModuleProviders.DaoProvider(daoClass);
        binder.bind(daoClass).toProvider(provider).asEagerSingleton();
    }

    /**
     * 绑定实体类<code>entityClass</code>和对应的dao<code>daoClass</code>
     *
     * @param binder      绑定器,非空
     * @param entityClass 实体类,非空
     * @param daoClass    实体类对应的Dao类,非空
     */
    public static void bindEntityAndDao(@Nonnull Binder binder, @Nonnull Class entityClass, @Nonnull Class daoClass) {
        Dao annotation = Preconditions.checkNotNull(AnnotationUtils.findAnnotation(daoClass, Dao.class), "Can't find annotation %s for daoClass %s", Dao.class, daoClass);
        Preconditions.checkArgument(annotation.entityClass() == entityClass, "The `%s` does not match dao class `%s`", entityClass, daoClass);
        Multibinder<Class> entityShardMultibinder = createEntityClassMutilbinder(binder);
        entityShardMultibinder.addBinding().toInstance(entityClass);

        bindDao(binder, daoClass);
    }

    /**
     * 取得指定类型并且指定名称的Set key
     *
     * @param clazz
     * @param name
     * @param <T>
     * @return
     */
    public static <T> Key<Set<T>> getSetKey(Class<T> clazz, String name) {
        Type type = Types.setOf(clazz);
        TypeLiteral<Set<T>> setTypeLiteral = (TypeLiteral<Set<T>>) TypeLiteral.get(type);
        return Key.get(setTypeLiteral, Names.named(name));
    }
}
