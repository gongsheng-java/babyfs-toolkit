package com.babyfs.tk.dal.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.babyfs.tk.dal.db.*;
import com.babyfs.tk.dal.db.shard.*;

import javax.sql.DataSource;
import java.util.Set;

/**
 * DalModule的各种{@link Provider}定义
 */
public class DalModuleProviders {
    protected DalModuleProviders() {

    }

    /**
     *
     */
    public static final class DaoProvider implements Provider<IDao> {
        private final Class daoClass;
        private DaoFactory daoFactory;

        public DaoProvider(Class daoClass) {
            this.daoClass = daoClass;
        }

        @Inject
        public void setDaoFactory(DaoFactory daoFactory) {
            this.daoFactory = daoFactory;
        }

        @Override
        public IDao get() {
            return daoFactory.buildDao(daoClass);
        }
    }

    /**
     * 构建DaoFactory
     */
    public static final class DaoFactoryProvider implements Provider<DaoFactory> {
        @Inject
        private DaoSupport daoSupport;

        @Override
        public DaoFactory get() {
            return new DaoFactory(daoSupport);
        }
    }

    /**
     * 构建DaoSupport
     */
    public static final class DaoSupportProvider implements Provider<DaoSupport> {
        @Inject
        private ShardDataSource dataSource;
        @Inject
        private EntityMetaSet entityMetaSet;
        @Inject
        @Named(DalShardModule.NAME_DB_ENTITY_SHARD_SET)
        private Set<EntityShard> shards;

        @Override
        public DaoSupport get() {
            DBObjectSet<EntityShard> shardDBObjectSet = new DBObjectSet<EntityShard>();
            for (EntityShard shard : shards) {
                shardDBObjectSet.add(shard);
            }
            return new DaoSupport(dataSource, entityMetaSet, shardDBObjectSet);
        }
    }

    /**
     * 构建数据源
     */
    public static final class ShardDataSourceProvider implements Provider<ShardDataSource> {
        @Inject(optional = true)
        @Named(DalShardModule.NAME_DEFAULT_DATASOURCE)
        private DataSource defaultDataSource;

        @Inject(optional = true)
        @Named(DalShardModule.NAME_DEFAULT_SHARD_GROUP)
        private String defaultShardGroup;

        @Inject(optional = true)
        @Named(DalShardModule.NAME_DEFAULT_SHARD_ID)
        private String defaultShardId;

        @Inject
        private ShardDataSourceContainer container;

        @Override
        public ShardDataSource get() {
            if (defaultDataSource != null) {
                return new ShardDataSource(container, defaultDataSource);
            }
            return new ShardDataSource(container, defaultShardGroup, defaultShardId);
        }
    }

    /**
     * 收集注册的所有实体类型,构建{@link EntityMetaSet}实例
     */
    public static final class EntityMetaSetProvider implements Provider<EntityMetaSet> {
        @Inject
        @Named(DalShardModule.NAME_ENTITY_CLASS_SET)
        private Set<Class> entityClassSet;

        @Override
        public EntityMetaSet get() {
            EntityMetaSet set = new EntityMetaSet();
            for (Class clazz : entityClassSet) {
                set.add(clazz);
            }
            return set;
        }
    }

    /**
     * 负责创建{@link ShardDataSourceContainer}的实例
     */
    public static final class ShardDataSourceContainerProvier implements Provider<ShardDataSourceContainer> {
        /**
         * 数据库实例的集合
         */
        @Inject
        @Named(DalShardModule.NAME_DB_INSTANCE_SET)
        private Set<DBInstance> dbInstanceSet;

        /**
         * Shard实例
         */
        @Inject
        @Named(DalShardModule.NAME_DB_SHARD_INSTANCE_SET)
        private Set<DBShardInstance> dbShardInstanceSet;

        /**
         * 连接池的创建器
         */
        @Inject
        private IDataSourceCreator dataSourceCreator;

        @Override
        public ShardDataSourceContainer get() {
            ShardDataSourceContainer container = new ShardDataSourceContainer(dataSourceCreator);
            Preconditions.checkState(dbInstanceSet != null && !(dbInstanceSet.isEmpty()), "dbInstanceSet");
            for (DBInstance instance : dbInstanceSet) {
                container.addDBInstance(instance);
            }
            if (dbShardInstanceSet != null) {
                for (DBShardInstance dbShardInstance : dbShardInstanceSet) {
                    container.addDBShardInstance(dbShardInstance);
                }
            }
            return container;
        }
    }
}
