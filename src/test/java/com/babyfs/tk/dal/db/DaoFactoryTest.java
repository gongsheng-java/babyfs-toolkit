package com.babyfs.tk.dal.db;

import com.google.common.base.Function;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.model.Friend;
import com.babyfs.tk.dal.db.model.IUserDao;
import com.babyfs.tk.dal.db.model.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/**
 */
public class DaoFactoryTest {

    @Test
    @Ignore
    public void testBuildDao() throws Exception {
        Method save = IUserDao.class.getMethod("save", new Class[]{IEntity.class});
        System.out.println(save);
    }

    @Test
    @Ignore
    public void testDao() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf-8", "root", "mysql");
        EntityMetaSet metaSet = new EntityMetaSet();
        metaSet.add(User.class);
        metaSet.add(Friend.class);
        DaoSupport daoSupport = new DaoSupport(dataSource, metaSet);
        DaoFactory daoFactory = new DaoFactory(daoSupport);
        final IUserDao userDao = daoFactory.buildDao(IUserDao.class);
        User user = new User();
        user.setName("wdy");
        userDao.save(user);
        user = new User();
        user.setName("李斌");
        userDao.save(user);
        System.out.println(userDao.hashCode());

        List<User> result = userDao.findUserByName("李斌", 2, 3);
        System.out.println(result);
        List result2 = userDao.findNameByName("李斌", 2, 3);
        System.out.println(result2);

        int count = userDao.findUser("李斌");
        System.out.println("count:" + count);

        int i = userDao.updateName("李斌", "李斌2");
        System.out.println("Updated count:" + i);
        int deleted = userDao.deleteName("李斌2");
        Assert.assertTrue(deleted > 0);
        deleted = userDao.deleteName("wdy");
        Assert.assertTrue(deleted > 0);
        System.out.println("deleted:" + deleted);

        //事务
        daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                User user = new User();
                user.setName("wdy2");
                userDao.save(user);
                System.out.println("uid:" + user.getId());
                User user2 = new User();
                user2.setName("wdy3");
                userDao.save(user2);
                int deleted = userDao.deleteName("wdy2");
                Assert.assertEquals(1, deleted);
                deleted = userDao.deleteName("wdy3");
                Assert.assertEquals(1, deleted);
                return null;
            }
        });
    }
}
