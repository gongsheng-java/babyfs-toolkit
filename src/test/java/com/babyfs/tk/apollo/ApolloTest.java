package com.babyfs.tk.apollo;


import com.alibaba.fastjson.JSON;
import com.babyfs.tk.apollo.guice.ApolloModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

@Ignore
public class ApolloTest {

    private final static Logger logger = LoggerFactory.getLogger(ApolloTest.class);


    @Test
    public void testJsonConfig(){
        Injector injector = Guice.createInjector(new ApolloModule());


        ApolloJsonTest apolloJsonTest = injector.getInstance(ApolloJsonTest.class);
        Assert.notNull(apolloJsonTest);
        logger.info("the result is : {}", JSON.toJSON(apolloJsonTest));

    }

    @Test
    public void testFieldConfig(){
        Injector injector = Guice.createInjector(new ApolloModule());
        TestConfig testConfig = injector.getInstance(TestConfig.class);
        Assert.notNull(testConfig);
        logger.info("the result is : {}", JSON.toJSON(testConfig));

    }

    static ApolloJsonTest test;
    static TestConfig test1;

    @Test
    public void testWatch(){
        ConfigLoader.watch(TestConfig.class, apolloTest -> {
            test1 = apolloTest;
            logger.info("after changed: {}", test1.getName());
        });

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testCompConfig(){
        System.setProperty("app.id", "testToolkit");
        Injector injector = Guice.createInjector(new ApolloModule());
        CompConfig testConfig = injector.getInstance(CompConfig.class);

        Assert.notNull(testConfig);
        logger.info(JSON.toJSONString(testConfig));
    }
}
