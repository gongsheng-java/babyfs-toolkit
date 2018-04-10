package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Preconditions;

/**
 * Shard的策略
 */
public enum ShardStrategyType {
    /**
     * 使用Hash取模的算法,例如 id % shard_count
     */
    HASH("hash"),
    /**
     * 指定区间的算法,例如[1,1000] -> shard_0
     */
    RANGE("range"),
    /**
     * 使用指定的名称
     */
    NAMED("named"),
    /**
     * 值匹配
     */
    VALUE_MATCH("value_match");

    private final String strategyName;

    ShardStrategyType(String strategyName) {
        this.strategyName = Preconditions.checkNotNull(strategyName);
    }

    /**
     * 是否匹配名称
     *
     * @param name
     * @return
     */
    public boolean isMatch(String name) {
        return strategyName.equals(name);
    }
}
