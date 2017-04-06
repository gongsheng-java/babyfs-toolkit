package com.babyfs.tk.dal.guice;

import com.google.inject.*;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.model.Friend;
import com.babyfs.tk.dal.db.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 */
public class GuiceStaticInjectTest {
    @Test
    public void test_static_inject() throws IOException {
        Module entityModulle = new AbstractModule() {
            Class<? extends IEntity> c = User.class;

            @Override
            protected void configure() {
                bind(User.class).toInstance(new User());
                bind(Friend.class).toInstance(new Friend());
                UserAndFriend userAndFriend = new UserAndFriend();
                bind(UserAndFriend.class).toInstance(userAndFriend);
                /*
                 * requestInjection 会导致userFriend的绑定失效,可以认为requestInjection的优先级更高,
                 * 如果一个对象被requestInjection了,那么他的之前的绑定将不起作用了,即一旦requestInjection完成后,
                 * 该对象将被从injector中移除,这个原因还需要验证.
                 * 可以确认的是: 对于一个instance,如果被绑定过了,那就不能再requestInjection,否则会抛出NPE异常
                 * requestInjection(userAndFriend);
                 */
                requestStaticInjection(StaticInject.class);
            }
        };
        Injector injector = Guice.createInjector(entityModulle);
        Assert.assertNotNull(injector);
    }

    public static class UserAndFriend {
        private User user;
        private Friend friend;

        @Inject
        public void setUser(User user) {
            this.user = user;
        }

        @Inject
        public void setFriend(Friend friend) {
            this.friend = friend;
        }
    }

    public static class StaticInject {
        @Inject
        private static void pleaseInject(UserAndFriend user) {
            Assert.assertNotNull(user);
        }
    }
}
