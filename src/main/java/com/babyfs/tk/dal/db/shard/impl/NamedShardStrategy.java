package com.babyfs.tk.dal.db.shard.impl;

import com.babyfs.tk.dal.db.shard.IShardStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 指定命名的shard策略
 */
public class NamedShardStrategy implements IShardStrategy {

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
