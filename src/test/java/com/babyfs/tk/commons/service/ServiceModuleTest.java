package com.babyfs.tk.commons.service;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ServiceModuleTest {
    private static final int ID = 3456;
    public static class TestModule extends ServiceModule {
        @Override
        protected void configure() {
            AddImpl add = new AddImpl(ID);
            bind(String.class).annotatedWith(Names.named("myName")).toInstance("d0ngw");
            bindService(IAdd.class, add);
            //requestInjection(add);
            requestStaticInjection(StaticInject.class);
        }
    }

    public static interface IAdd {
        public int add(int a, int b);

        public int getId();
    }

    public static class AddImpl implements IAdd {
        private final int id;
        
        private String name;

        public AddImpl(int id) {
            this.id = id;
        }

        @Inject(optional = true)
        public void setName(@Named("myName") String name){
            this.name = name;
        }

        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public int getId() {
            return this.id;
        }
    }

    public static class StaticInject {
        @Inject
        public static void logInject(IAdd add) {
            Assert.assertNotNull(add);
            Assert.assertEquals(ID,add.getId());
            System.out.println(add);
        }
    }

    @Test
    public void testInject() {
        Injector injector = Guice.createInjector(new TestModule());
        Assert.assertNotNull(injector);
    }
}
