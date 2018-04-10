package com.babyfs.tk.dal.db.shard.impl;

import com.babyfs.tk.dal.db.shard.IShardStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 使用数字区间的shard策略,即在[begin,end]区间内
 */
public class NumberRangeStrategy implements IShardStrategy {
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
