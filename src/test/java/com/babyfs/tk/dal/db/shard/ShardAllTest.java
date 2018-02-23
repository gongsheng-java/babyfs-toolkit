package com.babyfs.tk.dal.db.shard;

import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.dal.db.DaoSupport;
import com.babyfs.tk.dal.db.EntityMetaSet;
import com.babyfs.tk.dal.db.ShardDataSource;
import com.babyfs.tk.dal.db.model.IShardFriendDao;
import com.babyfs.tk.dal.db.model.ShardFriend;
import com.babyfs.tk.dal.db.shard.impl.HashShardStrategy;
import com.babyfs.tk.dal.db.shard.impl.NamedShardStrategy;
import com.babyfs.tk.dal.db.shard.impl.TomcatDataSourceCreator;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ShardAllTest {
    @Test
    @Ignore
    public void testDBNoShard_TableNoShard() {
        /*
       分库策略: 不分库
       分表策略: 不分表
        */
        ArrayList<NamedShardStrategy> dbShards = Lists.newArrayList(new NamedShardStrategy("test"));
        ArrayList<NamedShardStrategy> tablebShards = Lists.newArrayList(new NamedShardStrategy("friend"));
        test(dbShards, tablebShards);
    }

    @Test
    @Ignore
    public void testDBShard_TableNoShard() {
        /*
       分库策略: hash值 id%3
       分表策略: 不分表
        */
        ArrayList<HashShardStrategy> dbShards = Lists.newArrayList(new HashShardStrategy(3, "gsns"));
        ArrayList<NamedShardStrategy> tablebShards = Lists.newArrayList(new NamedShardStrategy("friend_shard"));
        test(dbShards, tablebShards);
    }

    @Test
    @Ignore
    public void testDBShard_TableShard() {
        /*
       分库策略: hash值 id%3
       分表策略: hash id%2
        */
        ArrayList<HashShardStrategy> dbShards = Lists.newArrayList(new HashShardStrategy(3, "gsns"));
        ArrayList<HashShardStrategy> tablebShards = Lists.newArrayList(new HashShardStrategy(2, "friend_shard"));
        test(dbShards, tablebShards);
    }

    private void test(List<? extends IShardStrategy> dbShards, List<? extends IShardStrategy> tableShards) {
        //setup db instance
        DBInstance dbInstance_remote = new DBInstance("db_0", "127.0.0.1", 3306, "root", "123456");
        ShardDataSourceContainer shardDataSourceContainer = new ShardDataSourceContainer(new TomcatDataSourceCreator());
        shardDataSourceContainer.addDBInstance(dbInstance_remote);

        //setup database shard
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("test", "db_0", "gsns_test", "test"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_0", "db_0", "gsns_test", "gsns"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_1", "db_0", "gsns_test", "gsns_test"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_2", "db_0", "gsns_test", "gsns_dev"));

        EntityMetaSet entityMetaSet = new EntityMetaSet();
        //setup entity class
        {
            entityMetaSet.add(ShardFriend.class);
        }

        //setup entityShard
        DBObjectSet<EntityShard> entityShardDBObjectSet = new DBObjectSet<EntityShard>();
        {
            {
                //setup ShardFriend
                EntityShard friendShard = new EntityShard(ShardFriend.class, "gsns_test", dbShards, tableShards);
                entityShardDBObjectSet.add(friendShard);
            }
        }

        //建立数据源
        ShardDataSource dataSource = new ShardDataSource(shardDataSourceContainer, null);
        DaoSupport daoSupport = new DaoSupport(dataSource, entityMetaSet, entityShardDBObjectSet);
        DaoFactory daoFactory = new DaoFactory(daoSupport);

        IShardFriendDao iShardFriendDao = daoFactory.buildDao(IShardFriendDao.class);

        testShardFriend(iShardFriendDao);
    }

    public static void testShardFriend(IShardFriendDao iShardFriendDao) {
        final int count = 10;

        //test insert
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = new ShardFriend();
            shardFriend.setId(i);
            shardFriend.setName("friend_" + (i));
            iShardFriendDao.save(shardFriend);
        }

        //test  get
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = iShardFriendDao.get((i), ShardFriend.class);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals("friend_" + i, shardFriend.getName());
        }

        //test update
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = new ShardFriend();
            shardFriend.setId(i);
            shardFriend.setName("friend_" + i + "_update");
            iShardFriendDao.update(shardFriend);
        }

        //check the update result
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = iShardFriendDao.get((i), ShardFriend.class);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals("friend_" + i + "_update", shardFriend.getName());
        }


        //test query
        for (int i = 1; i <= count; i++) {
            List<ShardFriend> shardFriends = iShardFriendDao.find((i));
            Assert.assertEquals(1, shardFriends.size());
            ShardFriend shardFriend = shardFriends.get(0);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals("friend_" + i + "_update", shardFriend.getName());
        }

        //test query columns
        for (int i = 1; i <= count; i++) {
            List<Object[]> shardFriends = iShardFriendDao.findColumns((i));
            Assert.assertEquals(1, shardFriends.size());
            Object[] shardFriend = shardFriends.get(0);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((long) i, shardFriend[0]);
            Assert.assertEquals("friend_" + i + "_update", shardFriend[1]);
            Assert.assertEquals(0, shardFriend[2]);
        }

        //test count
        for (int i = 1; i <= count; i++) {
            int count1 = iShardFriendDao.findCount((i));
            Assert.assertTrue(count1 > 0);
        }

        //test updae column
        for (int i = 1; i <= count; i++) {
            int i1 = iShardFriendDao.updateName(i, "new_name_friend_" + i);
            Assert.assertEquals(1, i1);
        }

        //check the update result
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = iShardFriendDao.get((i), ShardFriend.class);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals("new_name_friend_" + i, shardFriend.getName());
        }

        //test update only name
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = new ShardFriend();
            shardFriend.setId(i);
            shardFriend.setHeight(10);
            shardFriend.setName("friend_" + i + "only_update");
            iShardFriendDao.updateOnlyName(shardFriend);
        }

        //check the update result
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = iShardFriendDao.get((i), ShardFriend.class);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals(0, shardFriend.getHeight());
            Assert.assertEquals("friend_" + i + "only_update", shardFriend.getName());
        }

        //test update only name
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = new ShardFriend();
            shardFriend.setId(i);
            shardFriend.setHeight(10);
            shardFriend.setWeight(20);
            shardFriend.setName("friend_" + i + "all_update");
            iShardFriendDao.updateExcludeName(shardFriend);
        }

        //check the update result
        for (int i = 1; i <= count; i++) {
            ShardFriend shardFriend = iShardFriendDao.get((i), ShardFriend.class);
            Assert.assertNotNull(shardFriend);
            Assert.assertEquals((i), shardFriend.getId());
            Assert.assertEquals(10, shardFriend.getHeight());
            Assert.assertEquals(20, shardFriend.getWeight());
            Assert.assertEquals("friend_" + i + "only_update", shardFriend.getName());
        }

        //test delete
        for (int i = 1; i <= 5; i++) {
            ShardFriend shardFriend = new ShardFriend();
            shardFriend.setId(i);
            boolean delete = iShardFriendDao.delete(shardFriend);
            Assert.assertTrue(delete);
        }

        //test delete
        for (int i = 6; i <= 10; i++) {
            int delete = iShardFriendDao.delete(i);
            Assert.assertEquals(1, delete);
        }

        //test count
        for (int i = 1; i <= count; i++) {
            int count1 = iShardFriendDao.findCount((i));
            Assert.assertEquals(0, count1);
        }
    }
}
