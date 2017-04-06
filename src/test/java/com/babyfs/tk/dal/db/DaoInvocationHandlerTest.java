package com.babyfs.tk.dal.db;

import com.babyfs.tk.dal.db.annotation.*;
import com.babyfs.tk.dal.db.model.Counter;
import com.babyfs.tk.dal.db.model.Friend;
import com.babyfs.tk.dal.db.model.ICounterDao;
import com.babyfs.tk.dal.db.model.User;
import com.babyfs.tk.dal.db.shard.impl.TomcatDataSourceCreator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 */
public class DaoInvocationHandlerTest {
    private DaoSupport daoSupport;

    @Before
    @Ignore
    public void setUp() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        TomcatDataSourceCreator tomcatDataSourceCreator = new TomcatDataSourceCreator();
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf-8", "root", "mysql");
        EntityMetaSet metaSet = new EntityMetaSet();
        metaSet.add(User.class);
        metaSet.add(Friend.class);
        metaSet.add(Counter.class);
        daoSupport = new DaoSupport(dataSource, metaSet);
    }

    @Test
    public void testBuild() throws Exception {
        DaoInvocationHandler handler = new DaoInvocationHandler(IFriendEntityDao.class, daoSupport, new Class[]{IFriendEntityDao.class});
        Assert.assertNotNull(handler);

        try {
            new DaoInvocationHandler(IBadFriendEntityDao.class, daoSupport, new Class[]{IBadFriendEntityDao.class});
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        try {
            new DaoInvocationHandler(IBad2FriendEntityDao.class, daoSupport, new Class[]{IBad2FriendEntityDao.class});
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
    }

    @Test
    @Ignore
    public void testUpdateEntity() throws Exception {
        DaoFactory daoFactory = new DaoFactory(daoSupport);
        IFriendEntityDao friendEntityDao = daoFactory.buildDao(IFriendEntityDao.class);
        Friend friend = new Friend();
        friend.setId(1);
        friend.setHeight(1);
        friend.setWeight(1);
        friend.setName("朋友1");
        try {
            friendEntityDao.save(friend);
            long st = System.currentTimeMillis();

            Assert.assertFalse(friendEntityDao.updateFriend(friend, 2));

            friend.setWeight(2);
            Assert.assertTrue(friendEntityDao.updateFriend(friend, 1));

            Friend friendNew = friendEntityDao.get(1, Friend.class);
            Assert.assertEquals(friendNew.getWeight(), 2);
            Assert.assertEquals(friendNew.getHeight(), 1);

            friend.setWeight(3);
            friend.setHeight(2);
            Assert.assertTrue(friendEntityDao.updateFriend(friend, 2));

            friendNew = friendEntityDao.get(1, Friend.class);
            Assert.assertEquals(friendNew.getWeight(), 3);
            Assert.assertEquals(friendNew.getHeight(), 2);

            System.out.println(System.currentTimeMillis() - st);
        } finally {
            friendEntityDao.delete(friend);
        }
    }

    @Test
    @Ignore
    public void test_ExecDao() throws Exception {
        DaoFactory daoFactory = new DaoFactory(daoSupport);
        ICounterDao counterDao = daoFactory.buildDao(ICounterDao.class);
        int ret = counterDao.incr(1, 1, 1);
        System.out.println(ret);
        ret = counterDao.incr(2, 1, 1);
        System.out.println(ret);
    }

    @Dao(entityClass = Friend.class)
    public static interface IFriendEntityDao extends IDao<Friend> {
        @Sql(type = SqlType.UPDATE_ENTITY, condition = "weight = :pre_weight")
        boolean updateFriend(@EntityParam Friend entity, @SqlParam("pre_weight") int preWeight);
    }


    @Dao(entityClass = Friend.class)
    public static interface IBadFriendEntityDao extends IDao<Friend> {
        @Sql(type = SqlType.UPDATE_ENTITY, condition = "weight = :pre_weight")
        boolean updateFriend(@EntityParam @SqlParam(value = "test") Friend entity, @SqlParam("pre_weight") int preWeight);
    }

    @Dao(entityClass = Friend.class)
    public static interface IBad2FriendEntityDao extends IDao<Friend> {
        @Sql(type = SqlType.UPDATE_ENTITY, condition = "weight = :pre_weight")
        boolean updateFriend(@EntityParam Friend entity, @EntityParam int preWeight);
    }

}