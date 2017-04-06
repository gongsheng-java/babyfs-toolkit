package com.babyfs.tk.commons.service;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.List;
import java.util.Set;

/**
 * 将Guice的服务集成到Spring的BeanFactory中,可以在spring mvc中直接调用Guice module提供的服务
 * 需要注意的是,只有绑定到 {@link ServiceEnrty}的service才会被注册到spring中
 * <p/>
 *
 * @see {@link ServiceModule#bindService(Class, Class)}
 */
public class GuiceServiceSpringContext extends AbstractApplicationContext {
    private Guice2SpringBeanFactory factory;

    public GuiceServiceSpringContext(List<Module> modules) {
        this.factory = new Guice2SpringBeanFactory(modules);
        refresh();
    }

    @Override
    protected void refreshBeanFactory() throws BeansException, IllegalStateException {
    }

    @Override
    protected void closeBeanFactory() {
        factory.stop();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return this.factory;
    }

    private static final class Guice2SpringBeanFactory extends DefaultListableBeanFactory {
        private static final long serialVersionUID = 2349329435479653452L;
        private final transient Injector injector;

        public Guice2SpringBeanFactory(List<Module> modules) {
            List<Module> allModules = Lists.newArrayList();
            allModules.add(new LifecycleWebModule());
            allModules.addAll(modules);
            injector = Guice.createInjector(allModules);
            //注册Cuice Injector
            this.registerSingleton(GuiceInjector.GUICE_INJECTOR_BEAN_NAME, new GuiceInjector(injector));
            Set<ServiceEnrty> allServices = ServiceEnrty.getAllServices(injector);
            //注册所有的服务
            for (ServiceEnrty entry : allServices) {
                Object instance = injector.getInstance(entry.getGuiceKey());
                String annotatedName = entry.getAnnotatedName();
                if (entry.getAnnotatedName() == null) {
                    this.registerSingleton(entry.getClassName(), instance);
                } else {
                    // 如果是基于Named注解绑定的，直接使用注解的名字做为bean name
                    this.registerSingleton(annotatedName, instance);
                }
            }
            injector.getInstance(IContext.class).getInitActionRegistry().execute();
        }

        public void stop() {
            injector.getInstance(IContext.class).getShutdownActionRegistry().execute();
        }
    }
}
