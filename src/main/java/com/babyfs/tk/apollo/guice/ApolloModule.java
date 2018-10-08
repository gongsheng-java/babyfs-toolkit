package com.babyfs.tk.apollo.guice;

import com.babyfs.tk.apollo.ConfigLoader;
import com.babyfs.tk.apollo.EnvConstants;
import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ApolloModule  extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(ApolloModule.class);

    @Override
    protected void configure() {
        Reflections reflections = new Reflections(EnvConstants.AUTO_SCAN_PACKAGE);

        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(ApolloScan.class);

        for (Class<?> annotatedClass :
                classSet) {
            registerBean(annotatedClass);
        }
    }

    private void registerBean(Class tClass){
        Object config = ConfigLoader.getConfig(tClass);

        bind(tClass).toInstance(config);
    }

}
