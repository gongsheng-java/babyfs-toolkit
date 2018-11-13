package com.babyfs.tk.apollo.guice;

import com.babyfs.tk.apollo.ConfigLoader;
import com.babyfs.tk.apollo.EnvConstants;
import com.babyfs.tk.apollo.annotation.ApolloScan;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 */
public class ApolloModule  extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(ApolloModule.class);

    /**
     * scan all the class with annotation Apollo, and register them into ioc container
     */
    @Override
    protected void configure() {
        Reflections reflections = new Reflections(EnvConstants.AUTO_SCAN_PACKAGE);

        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(ApolloScan.class);

        for (Class<?> annotatedClass :
                classSet) {
            ApolloScan annotation = annotatedClass.getAnnotation(ApolloScan.class);
            registerBean(annotatedClass, annotation.value());
        }
    }

    /**
     * register a config class with apollo config loader
     * @param tClass
     */
    private void registerBean(Class tClass, String namespace){
        Object config = ConfigLoader.getConfig(namespace, tClass);
        if(config == null){
            logger.warn("unable to get config: #name space {}, #className {}", namespace, tClass.getName());
            return;
        }
        bind(tClass).toInstance(config);
    }

}
