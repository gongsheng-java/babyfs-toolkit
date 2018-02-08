package com.babyfs.tk.dal.db.shard;

import java.util.Map;

/**
 */
public interface IShardStrategy {
    String SHARD_NAME = "shardName";

    /**
     * 指定的值是否与该策略匹配
     *
     * @param value
     * @return
     */
    boolean isMatch(Map<String, Object> value);

    /**
     * 取得指定数据对应的shard name
     *
     * @param value
     * @return
     */
    String getShardName(Map<String, Object> value);
}
