package com.babyfs.tk.apollo;


import com.babyfs.tk.apollo.guice.ApolloModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.springframework.util.Assert;

import javax.inject.Inject;

public class ApolloTest {

    @Test
    public void testConfigLoad(){
        System.setProperty("app.id", "testToolkit");
        Injector injector = Guice.createInjector(new ApolloModule());
        TestConfig testConfig = injector.getInstance(TestConfig.class);
        Assert.notNull(testConfig);

        ApolloJsonTest apolloJsonTest = injector.getInstance(ApolloJsonTest.class);
        Assert.notNull(apolloJsonTest);

    }
}
