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
        throw new RuntimeException("Can't find the db shard name for value [" + value + "],entityClass" + entityClass);
    }

    /**
     * 取得指定<code>value</code>的table shard名称
     *
     * @param value
     * @return
     * @throws RuntimeException 无法查找到对应的table shard会抛出此异常
     */
    public String findTableShardName(@Nonnull Map<String, Object> value) {
        for (IShardStrategy strategy : this.tableShardStragies) {
            if (strategy.isMatch(value)) {
                return strategy.getShardName(value);
            }
        }
        throw new RuntimeException("Can't find the table shard name for value [" + value + "],entityClass" + entityClass);
    }

    @Override
    public String getId() {
        return entityClass.getName();
    }

    @Override
    public int getSeqId() {
        return this.seqId;
    }

    /**
     * Shard的策略
     */
    public static enum ShardStrategyType {
        /**
         * 使用Hash取模的算法,例如 id % shard_count
         */
        HASH,
        /**
         * 指定区间的算法,例如[1,1000] -> shard_0
         */
        RANGE
    }

    /**
     */
    public static interface IShardStrategy {
        /**
         * 指定的值是否与该策略匹配
         *
         * @param value
         * @return
         */
        public boolean isMatch(Map<String, Object> value);

        /**
         * 取得指定数据对应的shard name
         *
         * @param value
         * @return
         */
        public String getShardName(Map<String, Object> value);
    }

    /**
     * 指定命名的shard策略
     */
    public static class NamedShardStrategy implements IShardStrategy {
        private final String shardName;

        /**
         * @param shardName 指定的shard名称
         */
        public NamedShardStrategy(@Nonnull String shardName) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(shardName), "shardName");
            this.shardName = shardName;
        }

        @Override
        public boolean isMatch(Map<String, Object> value) {
            return true;
        }

        @Override
        public String getShardName(Map<String, Object> value) {
            return shardName;
        }
    }

    /**
     * 使用Hash算法的shard策略
     */
    public static class HashShardStrategy implements IShardStrategy {
        private final int shardCount;
        private final String shardNamePrefix;
        private final String valueName;

        /**
         * @param shardCount
         * @param shardNamePrefix
         */
        public HashShardStrategy(@Nonnegative int shardCount, @Nonnull String shardNamePrefix) {
            this(shardCount, shardNamePrefix, "id");
        }

        /**
         * @param shardCount      shard的个数,必须大于0
         * @param shardNamePrefix shard名称的前缀
         * @param valueName       {@link #isMatch(Map)} map中用于确定shard名称的key名
         */
        public HashShardStrategy(@Nonnegative int shardCount, @Nonnull String shardNamePrefix, String valueName) {
            Preconditions.checkArgument(shardCount > 0, "shardCount");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(shardNamePrefix), "shardNamePrefix");
            this.shardCount = shardCount;
            this.shardNamePrefix = shardNamePrefix;
            this.valueName = valueName;
        }

        @Override
        public boolean isMatch(Map<String, Object> value) {
            return value.containsKey(this.valueName);
        }

        @Override
        public String getShardName(Map<String, Object> value) {
            long l = ((Number) value.get(this.valueName)).longValue();
            Preconditions.checkArgument(l > 0, "value  %s must be > 0", value);
            return shardNamePrefix + "_" + (l % shardCount);
        }
    }

    /**
     * 使用数字区间的shard策略,即在[begin,end]区间内
     */
    public static class NumberRangeStrategy implements IShardStrategy {
        private final long begin;
        private final long end;
        private final String shardName;
        private final String valeName;

        public NumberRangeStrategy(@Nonnegative long begin, @Nonnegative long end, @Nonnull String shardName) {
            this(begin, end, shardName, "id");
        }

        public NumberRangeStrategy(@Nonnegative long begin, @Nonnegative long end, @Nonnull String shardName, @Nonnull String valueName) {
            Preconditions.checkArgument(begin > 0, "begin");
            Preconditions.checkArgument(end > 0, "end");
            Preconditions.checkArgument(begin <= end, "The begin(%s) should <= end(%s).", begin, end);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(shardName), "shardName");
            this.begin = begin;
            this.end = end;
            this.shardName = shardName;
            this.valeName = valueName;
        }

        @Override
        public boolean isMatch(Map<String, Object> value) {
            if (value.containsKey(this.valeName)) {
                long l = ((Number) value.get(this.valeName)).longValue();
                return l >= begin && l <= end;
            }
            return false;
        }

        @Override
        public String getShardName(Map<String, Object> value) {
            return shardName;
        }
    }

    /**
     * shard策略:名称匹配
     */
    public static class NameMatchShardStrategy implements IShardStrategy {
        private final String shardName;
        private final String valueName;
        private final String value;

        /**
         * @param shardName shard名称
         * @param valueName value的名称
         * @param value     value值
         */
        public NameMatchShardStrategy(@Nonnull String shardName, @Nonnull String valueName, String value) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(shardName), "shardName");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(valueName), "valueName");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "value");
            this.shardName = shardName;
            this.valueName = valueName;
            this.value = value;
        }

        @Override
        public boolean isMatch(Map<String, Object> value) {
            if (value == null || value.isEmpty() || !value.containsKey(this.valueName)) {
                return false;
            }
            return Objects.equals(this.value, value.get(this.valueName));
        }

        @Override
        public String getShardName(Map<String, Object> value) {
            return shardName;
        }
    }
}
