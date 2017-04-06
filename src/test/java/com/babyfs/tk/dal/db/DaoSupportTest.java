package com.babyfs.tk.dal.db;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.db.model.Friend;
import com.babyfs.tk.dal.db.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 */
public class DaoSupportTest {

    private DaoSupport daoSupport;

    @Before
    @Ignore
    public void setUp() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf-8", "root", "mysql");
        EntityMetaSet metaSet = new EntityMetaSet();
        metaSet.add(User.class);
        metaSet.add(Friend.class);

        System.out.println("begin");
        daoSupport = new DaoSupport(dataSource, metaSet);
        System.out.println("daoSupport");
    }

    @Test
    @Ignore
    public void testTransaction() {

        final Integer c1 = daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Integer>() {
            @Override
            public Integer apply(Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                return input.first.getJdbcOperations().queryForObject("select count(*) from user", Integer.class);
            }
        });

        int i = daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Integer>() {
            @Override
            public Integer apply(Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                JdbcOperations jdbcTemplate = input.first.getJdbcOperations();
                return jdbcTemplate.update("insert into user(name) values(?)", "胡晓晴");
            }
        });

        assertEquals(1, i);

        final Integer c2 = daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Integer>() {
            @Override
            public Integer apply(Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                return input.first.getJdbcOperations().queryForObject("select count(*) from user", Integer.class);
            }
        });

        assertEquals(i, c2 - c1);

        try {
            daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Object>() {
                @Override
                public Object apply(Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                    JdbcOperations jdbcTemplate = input.first.getJdbcOperations();
                    int c1 = jdbcTemplate.queryForObject("select count(*) from user", Integer.class);
                    jdbcTemplate.update("insert into user(name) values(?)", "胡晓晴3");
                    jdbcTemplate.update("insert into user(name) values(?)", "胡晓晴4");
                    int c2 = jdbcTemplate.queryForObject("select count(*) from user", Integer.class);
                    assertEquals(2, c2 - c1);
                    input.second.setRollbackOnly();
                    return null;
                }
            });
        } catch (Exception e) {

        }

        final Integer c3 = daoSupport.doTransaction(User.class, null, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Integer>() {
            @Override
            public Integer apply(Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                return input.first.getJdbcOperations().queryForObject("select count(*) from user", Integer.class);
            }
        });

        assertEquals(c2, c3);

    }

    @Test
    @Ignore
    public void testExposeJDBC_QueryForList() {
        User user = new User();
        user.setName("张阳");
        User saveUser = daoSupport.save(user);

        Friend friend1 = new Friend();
        friend1.setId(1);
        friend1.setHeight(1);
        friend1.setWeight(1);
        friend1.setName("张阳");
        Friend savedFriend1 = daoSupport.save(friend1);


        Friend friend2 = new Friend();
        friend2.setId(2);
        friend2.setHeight(170);
        friend2.setWeight(63);
        friend2.setName("张阳");
        Friend savedFriend2 = daoSupport.save(friend2);

        // 请根据自己的需要决定Function的返回值
        Void rt = daoSupport.exposeJDBCTemplate(User.class, null, new Function<NamedParameterJdbcOperations, Void>() {
            @Override
            public Void apply(NamedParameterJdbcOperations input) {
                JdbcOperations jdbc = input.getJdbcOperations();
                int rst1 = jdbc.queryForObject("select count(*) from user", Integer.class);
                System.out.println(rst1);

                List<Map<String, Object>> rst2 = jdbc.queryForList("select count(*) from user");
                System.out.println(rst2);

                // 测试条件参数
                List<Map<String, Object>> rst3 = jdbc.queryForList(
                        "select * from gsns_dev.user as a ,gsns_dev.friend as b where a.name = b.name and b.height=? and b.weight=?", new Object[]{170, 63});
                System.out.println(rst3);
                Assert.assertEquals(170, rst3.get(0).get("height"));
                Assert.assertEquals(63, rst3.get(0).get("weight"));


                // 测试跨schema
                List<Map<String, Object>> rst4 = jdbc.queryForList(
                        "select a.id as id,a.name as name1,b.name as name2 from gsns.user as a,gsns_dev.user as b where a.id = b.id and a.name=b.name");
                System.out.println(rst4);
                return null;
            }
        });

        {
            List<Map<String, Object>> rst3 = daoSupport.queryForList(Friend.class, null,
                    "select * from gsns_dev.user as a ,gsns_dev.friend as b where a.name = b.name and b.height=? and b.weight=?", 170, 63);
            System.out.println(rst3);
            Assert.assertEquals(170, rst3.get(0).get("height"));
            Assert.assertEquals(63, rst3.get(0).get("weight"));
        }

        {
            List<Map<String, Object>> rst3 = daoSupport.queryForList(Friend.class, null,
                    "select * from gsns_dev.user as a ,gsns_dev.friend as b where a.name = b.name and b.height=? and b.weight=?", new Object[]{170, 63});
            System.out.println(rst3);
            Assert.assertEquals(170, rst3.get(0).get("height"));
            Assert.assertEquals(63, rst3.get(0).get("weight"));
        }

        {
            Map<String, Object> params = Maps.newHashMap();
            params.put("height", 170);
            params.put("width", 63);
            List<Map<String, Object>> rst3_1 = daoSupport.queryForList(Friend.class, null,
                    "select * from gsns_dev.user as a ,gsns_dev.friend as b where a.name = b.name and b.height=:height and b.weight=:width", params);
            System.out.println(rst3_1);
            System.out.println(rst3_1.size());
            Assert.assertEquals(170, rst3_1.get(0).get("height"));
            Assert.assertEquals(63, rst3_1.get(0).get("weight"));
        }

        List<Map<String, Object>> rst5 = daoSupport.queryForList(User.class, null,
                "select * from gsns_dev.user as a ,gsns_dev.friend as b where a.name = b.name and b.height=? and b.weight=?", new Object[]{170, 63});
        System.out.println(rst5);
        Assert.assertEquals(170, rst5.get(0).get("height"));
        Assert.assertEquals(63, rst5.get(0).get("weight"));

        List<Map<String, Object>> rst6 = daoSupport.queryForList(User.class, null,
                "select a.id as id,a.name as name1,b.name as name2 from gsns.user as a,gsns_dev.user as b where a.id = b.id and a.name=b.name");
        System.out.println(rst6);

        daoSupport.delete(saveUser);
        daoSupport.delete(savedFriend1);
        daoSupport.delete(savedFriend2);
    }


    @Test
    @Ignore
    public void test() throws ClassNotFoundException {
        {
            User u0 = new User();
            u0.setName("王东永");
            User save0 = daoSupport.save(u0);
            Assert.assertNotNull(save0);
            Assert.assertTrue(save0.getId() > 0);
            User get0 = daoSupport.get(save0.getId(), User.class);
            Assert.assertNotNull(get0);
            Assert.assertEquals(save0.getId(), get0.getId());
            Assert.assertEquals(save0.getName(), get0.getName());
            get0.setName("李斌X饶军X张超");
            boolean update = daoSupport.update(get0);
            Assert.assertTrue(update);
            User get1 = daoSupport.get(save0.getId(), User.class);
            Assert.assertNotNull(get1);
            Assert.assertEquals(get0.getId(), get1.getId());
            Assert.assertEquals(get0.getName(), get1.getName());
            boolean delete = daoSupport.delete(get0);
            Assert.assertTrue(delete);
            User get2 = daoSupport.get(get0.getId(), User.class);
            Assert.assertNull(get2);
        }

        {
            Friend friend = new Friend();
            friend.setHeight(1);
            friend.setWeight(1);
            friend.setName("朋友1");
            try {
                daoSupport.save(friend);
                throw new IllegalStateException("Not reach.");
            } catch (IllegalArgumentException e) {

            }
            friend.setId(1);
            daoSupport.save(friend);
            Friend friend1 = daoSupport.get(1, Friend.class);
            Assert.assertNotNull(friend1);
            Assert.assertEquals(friend.getId(), friend1.getId());
            Assert.assertEquals(friend.getName(), friend1.getName());
            Assert.assertEquals(friend.getHeight(), friend1.getHeight());
            Assert.assertEquals(friend.getWeight(), friend1.getWeight());

            friend1.setName("Friend2");
            Assert.assertTrue(daoSupport.update(friend1));
            Assert.assertTrue(daoSupport.delete(friend1));
        }
    }

    @Test
    @Ignore
    public void testUpdateEntity() throws Exception {
        Friend friend = new Friend();
        friend.setId(1);
        friend.setHeight(1);
        friend.setWeight(1);
        friend.setName("朋友1");
        try {
            daoSupport.save(friend);

            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("pre_weight", 2);
            Assert.assertFalse(daoSupport.update(friend, " weight = :pre_weight", new MapSqlParameterSource(parameters)));

            parameters.put("pre_weight", 1);
            friend.setWeight(2);
            Assert.assertTrue(daoSupport.update(friend, " weight = :pre_weight", new MapSqlParameterSource(parameters)));

            parameters.put("pre_weight", 2);
            friend.setWeight(3);
            Assert.assertTrue(daoSupport.update(friend, " weight = :pre_weight", new MapSqlParameterSource(parameters)));

            Friend friendNew = daoSupport.get(1, Friend.class);
            Assert.assertEquals(friendNew.getWeight(), 3);
        } finally {
            daoSupport.delete(friend);
        }
    }
}
