package com.babyfs.tk.dal.db.shard.impl;

import com.babyfs.tk.dal.db.shard.IShardStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 使用Hash算法的shard策略
 */
public class HashShardStrategy implements IShardStrategy {
    public static final String SHARD_NAME_PREFIX = "shardNamePrefix";
    public static final String SHARD_COUNT = "shardCount";

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
