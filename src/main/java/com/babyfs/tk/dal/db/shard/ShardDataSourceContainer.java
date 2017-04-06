package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Strings;
import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.babyfs.tk.dal.db.ShardDataSource;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * shard环境中使用的DataSource提供者
 *
 * @see {@link ShardDataSource}
 */
public class ShardDataSourceContainer {
    private final IDataSourceCreator dataSourceCreator;
    /**
     * 数据库的实例
     */
    private DBObjectSet<DBInstance> dbInstanceSet = new DBObjectSet<DBInstance>();
    /**
     * Shard组的配置
     */
    private ConcurrentMap<String, DBObjectSet<DBShardInstance>> dbShardGroup = Maps.newConcurrentMap();
    /**
     * shard的数据源,key:Pair[shardGroup,shardId],value:javax.sql.DataSource
     */
    private LoadingCache<DataSourceKey, DataSource> shardDataSources = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(new DataSourceMapEvicitionListener()).build(new DataSourceFunction());

    /**
     * @param dataSourceCreator
     */
    public ShardDataSourceContainer(@Nonnull IDataSourceCreator dataSourceCreator) {
        checkArgument(dataSourceCreator != null, "dataSourceCreator");
        this.dataSourceCreator = dataSourceCreator;
    }

    /**
     * 增加一个数据库的实例
     *
     * @param instance
     */
    public synchronized void addDBInstance(@Nonnull DBInstance instance) {
        checkArgument(instance != null, "instance");
        dbInstanceSet.add(instance);
    }

    /**
     * 重新加载数据的实例
     *
     * @param instances
     */
    public synchronized void reloadDBInstance(@Nonnull Iterable<DBInstance> instances) {
        checkArgument(instances != null, "instances");
        dbInstanceSet.reload(instances);
    }

    /**
     * @param instance
     */
    public synchronized void addDBShardInstance(DBShardInstance instance) {
        String shardGroup = instance.getGroupName();
        DBObjectSet<DBShardInstance> dbShards = dbShardGroup.get(shardGroup);
        if (dbShards == null) {
            dbShards = new DBObjectSet<DBShardInstance>();
            DBObjectSet<DBShardInstance> preShard = dbShardGroup.putIfAbsent(shardGroup, dbShards);
            if (preShard != null) {
                dbShards = preShard;
            }
        }
        dbShards.add(instance);
    }

    /**
     * 重新加载指定<code>shardGroup</code>的DBShard的配置
     *
     * @param shardGroup shard的组名
     * @param instances  该shardGroup下的所有shard实例
     */
    public synchronized void reloadDBShard(String shardGroup, Iterable<DBShardInstance> instances) {
        DBObjectSet<DBShardInstance> dbShards = dbShardGroup.get(shardGroup);
        checkArgument(dbShards != null, "Can't find the %s shard group", shardGroup);
        dbShards.reload(instances);
    }

    /**
     * 取得指定shard名称的shard集合
     *
     * @param shardGroupName
     * @return
     */
    public DBObjectSet<DBShardInstance> getShards(String shardGroupName) {
        return dbShardGroup.get(shardGroupName);
    }

    /**
     * 取得指定shard的数据源
     *
     * @param shardGroup
     * @param shardId
     */
    public DataSource getDataSource(@Nonnull final String shardGroup, @Nonnull final String shardId) {
        checkArgument(!Strings.isNullOrEmpty(shardGroup), "shardGroup");
        checkArgument(!Strings.isNullOrEmpty(shardId), "shardId");
        DBObjectSet<DBShardInstance> dbShards = dbShardGroup.get(shardGroup);
        checkNotNull(dbShards, "Can't find the shard group %s", shardGroup);
        DBShardInstance dbShard = dbShards.get(shardId);
        checkNotNull(dbShard, "Can't find the shard [%s] from group[%s]", shardId, shardGroup);
        DBInstance dbInstance = dbInstanceSet.get(dbShard.getDbInstancId());
        DataSourceKey dataSourceKey = new DataSourceKey(shardGroup, shardId, dbInstance.getSeqId(), dbShard.getSeqId());
        return this.shardDataSources.getUnchecked(dataSourceKey);
    }

    /**
     * DataSource 的key
     */
    private static final class DataSourceKey {
        private final String shardGroup;
        private final String shardId;
        private final int dbSeqId;
        private final int shardSeqId;
        private final int hashCode;

        private DataSourceKey(@Nonnull String shardGroup, @Nonnull String shardId, int dbSeqId, int shardSeqId) {
            this.shardGroup = shardGroup;
            this.shardId = shardId;
            this.dbSeqId = dbSeqId;
            this.shardSeqId = shardSeqId;
            this.hashCode = genHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DataSourceKey that = (DataSourceKey) o;

            if (dbSeqId != that.dbSeqId) {
                return false;
            }
            if (shardSeqId != that.shardSeqId) {
                return false;
            }
            if (!shardGroup.equals(that.shardGroup)) {
                return false;
            }
            if (!shardId.equals(that.shardId)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        private int genHashCode() {
            int result = shardGroup.hashCode();
            result = 31 * result + shardId.hashCode();
            result = 31 * result + dbSeqId;
            result = 31 * result + shardSeqId;
            return result;
        }
    }

    /**
     * 创建DataSource
     */
    private final class DataSourceFunction extends CacheLoader<DataSourceKey, DataSource> {
        @Override
        public DataSource load(DataSourceKey key) throws Exception {
            String shardGroup = key.shardGroup;
            String shardId = key.shardId;
            DBObjectSet<DBShardInstance> dbShards = dbShardGroup.get(shardGroup);
            checkNotNull(dbShards, "Can't find the shard group %s", shardGroup);
            DBShardInstance dbShard = dbShards.get(shardId);
            checkNotNull(dbShard, "Can't find the shard group %s:%s ", shardGroup, shardId);
            DBInstance instance = dbInstanceSet.get(dbShard.getDbInstancId());
            return dataSourceCreator.create(instance.getIp(), instance.getPort(), instance.getUser(), instance.getPassword(), dbShard.getSchema(), dbShard.getParamters());
        }
    }

    /**
     *
     */
    private final class DataSourceMapEvicitionListener implements RemovalListener<DataSourceKey, DataSource> {
        @Override
        public void onRemoval(RemovalNotification<DataSourceKey, DataSource> notification) {
            DataSource value = notification.getValue();
            if (value != null) {
                dataSourceCreator.shutdown(value);
            }
        }
    }
}
