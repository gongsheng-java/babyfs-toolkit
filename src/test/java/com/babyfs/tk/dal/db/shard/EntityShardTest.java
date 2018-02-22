package com.babyfs.tk.dal.db.shard;

import com.babyfs.tk.dal.db.shard.impl.HashShardStrategy;
import com.babyfs.tk.dal.db.shard.impl.NumberRangeStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.dal.db.model.Friend;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * EntityShard的测试用例
 */
public class EntityShardTest {
    @Test
    public void test() {
        NumberRangeStrategy dbRangeStrategy = new NumberRangeStrategy(1000, 1000, "gsns_big");
        ArrayList<IShardStrategy> dbShards = Lists.newArrayList(dbRangeStrategy, new HashShardStrategy(1, "gsns"));

        NumberRangeStrategy tableRangeStrategy = new NumberRangeStrategy(1000, 1000, "friend_big_1000");
        ArrayList<IShardStrategy> tableShards = Lists.newArrayList(tableRangeStrategy, new HashShardStrategy(1, "friend"));
        EntityShard entityShard = new EntityShard(Friend.class, "gsns_test", dbShards, tableShards);

        Friend friend = new Friend();
        {
            //测试hash算法
            friend.setId(101);
            Map<String, Object> shardValue = Maps.newHashMap();
            shardValue.put("id", friend.getId());
            String dbShardName = entityShard.findDBShardName(shardValue);
            Assert.assertEquals("gsns_0", dbShardName);
            String tableShardName = entityShard.findTableShardName(shardValue);
            Assert.assertEquals("friend_0", tableShardName);

            friend.setId(100);
            shardValue.put("id", friend.getId());
            dbShardName = entityShard.findDBShardName(shardValue);
            Assert.assertEquals("gsns_0", dbShardName);
            tableShardName = entityShard.findTableShardName(shardValue);
            Assert.assertEquals("friend_0", tableShardName);
        }

        {
            //测试range算法
            friend.setId(1000);
            Map<String, Object> shardValue = Maps.newHashMap();
            shardValue.put("id", friend.getId());
            String dbShardName = entityShard.findDBShardName(shardValue);
            Assert.assertEquals("gsns_big", dbShardName);
            String tableShardName = entityShard.findTableShardName(shardValue);
            Assert.assertEquals("friend_big_1000", tableShardName);
        }
    }
}
