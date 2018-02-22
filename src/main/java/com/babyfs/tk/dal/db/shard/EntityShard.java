package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.babyfs.tk.dal.orm.IEntity;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 实体对象{@link IEntity} 的shard配置
 */
public class EntityShard implements IDBObject {
    private final Class<? extends IEntity> entityClass;
    private final int seqId;
    private final String dbShardGroup;
    private final List<IShardStrategy> dbShardStrategies;
    private final List<IShardStrategy> tableShardStragies;


    /**
     * @param entityClass  实体的类型
     * @param dbShardGroup DB Shard组名
     */
    public EntityShard(@Nonnull Class<? extends IEntity> entityClass, @Nonnull String dbShardGroup, Iterable<? extends IShardStrategy> dbShardStrategies, Iterable<? extends IShardStrategy> tableShardStrategis) {
        Preconditions.checkArgument(entityClass != null, "entityClass");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dbShardGroup), "dbShardGroup");
        this.entityClass = entityClass;
        this.seqId = ShardUtil.SEQUENCE.incrementAndGet();
        this.dbShardGroup = dbShardGroup;
        this.dbShardStrategies = Lists.newArrayList(dbShardStrategies);
        this.tableShardStragies = Lists.newArrayList(tableShardStrategis);
    }

    /**
     * 取得shard的实体类型
     *
     * @return
     */
    public Class<? extends IEntity> getEntityClass() {
        return entityClass;
    }

    /**
     * 取得实体类型的shard组名称
     *
     * @return
     */
    public String getDbShardGroup() {
        return dbShardGroup;
    }

    /**
     * 取得指定<code>value</code>的db shard名称
     *
     * @param value
     * @return
     * @throws RuntimeException 无法查找到对应的db shard会抛出此异常
     */
    public String findDBShardName(@Nonnull Map<String, Object> value) {
        for (IShardStrategy strategy : this.dbShardStrategies) {
            if (strategy.isMatch(value)) {
                return strategy.getShardName(value);
            }
        }
        return null;
    }

    /**
     * 取得指定<code>value</code>的table shard名称
     *
     * @param value
     * @return
     */
    public String findTableShardName(@Nonnull Map<String, Object> value) {
        for (IShardStrategy strategy : this.tableShardStragies) {
            if (strategy.isMatch(value)) {
                return strategy.getShardName(value);
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return entityClass.getName();
    }

    @Override
    public int getSeqId() {
        return this.seqId;
    }

}
