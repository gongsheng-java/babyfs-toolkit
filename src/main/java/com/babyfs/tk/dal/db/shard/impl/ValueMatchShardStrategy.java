package com.babyfs.tk.dal.db.shard.impl;

import com.babyfs.tk.dal.db.shard.IShardStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * shard策略:值匹配匹配
 */
public class ValueMatchShardStrategy implements IShardStrategy {
    public static final String Value_NAME = "valueName";
    public static final String VALUE = "value";

    private final String shardName;
    private final String valueName;
    private final String value;

    /**
     * @param shardName shard名称
     * @param valueName value的名称
     * @param value     value值
     */
    public ValueMatchShardStrategy(@Nonnull String shardName, @Nonnull String valueName, String value) {
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
