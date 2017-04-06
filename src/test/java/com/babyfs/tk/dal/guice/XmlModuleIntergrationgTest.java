package com.babyfs.tk.dal.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.babyfs.tk.commons.guice.GuiceGrapher;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.dal.db.model.IShardFriendDao;
import com.babyfs.tk.dal.db.model.IUserDao;
import com.babyfs.tk.dal.db.model.ShardFriend;
import com.babyfs.tk.dal.db.model.User;
import com.babyfs.tk.dal.db.shard.DBInstance;
import com.babyfs.tk.dal.db.shard.DBShardInstance;
import com.babyfs.tk.dal.db.shard.EntityShard;
import com.babyfs.tk.dal.db.shard.ShardAllTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

/**
 */
public class XmlModuleIntergrationgTest {


    @Test
    @Ignore
    public void test_conf() throws IOException {
        DalXmlConfModule dalXmlConfModule = new DalXmlConfModule("db_instance.xml", "shard_instance.xml", "entity_shards.xml");
        DalShardModule dalShardModule = new DalShardModule();
        Module entityModulle = new AbstractModule() {
            Class<? extends IEntity> c = User.class;

            @Override
            protected void configure() {
                Multibinder<Class> entityShardMultibinder = DalShardModule.createEntityClassMutilbinder(binder());
                entityShardMultibinder.addBinding().toInstance(User.class);
                entityShardMultibinder.addBinding().toInstance(ShardFriend.class);
                DalShardModule.bindDao(binder(), IShardFriendDao.class);
                DalShardModule.bindDao(binder(), IUserDao.class);
            }
        };
        Injector injector = Guice.createInjector(dalXmlConfModule, dalShardModule, entityModulle);
        {
            Set<DBInstance> instance = injector.getInstance(DalShardModule.DB_INSTANCE_SET_KEY);
            Assert.assertNotNull(instance);
            System.out.println(instance);
        }
        {

            Set<DBShardInstance> instance = injector.getInstance(DalShardModule.DB_SHARD_INSTANCE_SET_KEY);
            Assert.assertNotNull(instance);
            System.out.println(instance);
        }
        {
            Set<EntityShard> instance = injector.getInstance(DalShardModule.ENTITY_SHARD_SET_KEY);
            Assert.assertNotNull(instance);
            Assert.assertFalse(instance.isEmpty());
            System.out.println(instance);
        }
        GuiceGrapher.graph("dalXmlIntergation.dot", injector);
        DaoFactory factory = injector.getInstance(DaoFactory.class);
        Assert.assertNotNull(factory);
        System.out.println(factory);

        IShardFriendDao friendDao = injector.getInstance(IShardFriendDao.class);
        Assert.assertNotNull(friendDao);
        System.out.println("frendDao" + friendDao);
        ShardAllTest.testShardFriend(friendDao);

        IUserDao userDao = injector.getInstance(IUserDao.class);
        Assert.assertNotNull(userDao);
        System.out.println("userDao:" + userDao);
        User user = new User();
        user.setName("wdy");
        userDao.save(user);
    }


}
