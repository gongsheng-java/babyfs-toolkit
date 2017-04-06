package com.babyfs.tk.dubbo.guice;

import com.alibaba.dubbo.config.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.service.annotation.InitStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于Dubbo实现的RPC封装:
 * <dl>
 * <dt>
 * {@link com.alibaba.dubbo.config.ServiceConfig}
 * </dt>
 * <dd>
 * {@link com.alibaba.dubbo.config.ServiceConfig#ref}的实现实例,由Guice提供并注入到ref
 * </dd>
 * <dd>
 * 在Dubbo的配置文件中,不能直接申明实现的实例,使用{@value #SERVICE_CONFIG_REF_GUICE}[name]的方式引用在Guice中绑定的实例
 * </dd>
 * <dd>
 * ServcieConfig通过{@link InitStage}负责启动
 * </dd>
 * <dt>
 * {@link com.alibaba.dubbo.config.ReferenceConfig}
 * </dt>
 * <dd>
 * {@link com.alibaba.dubbo.config.ReferenceConfig}做为{@link com.alibaba.dubbo.config.ReferenceConfig#getInterfaceClass()}的实现绑定到Guice中
 * </dd>
 * <dd>
 * 用于绑定的key:
 * <code>
 * Key  guiceBindKey = Key.get(interfaceClass, Names.named(id));
 * </code>
 * </dd>
 * <dd>
 * 在Guice中引用的时候需要指定{@link com.google.inject.name.Named}
 * </dd>
 * </dl>
 */
public class DubboRPCModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRPCModule.class);
    /**
     * Dubbo{@link com.alibaba.dubbo.config.ServiceConfig#ref}引用Guice Service的前缀
     */
    public static final String SERVICE_CONFIG_REF_GUICE = "guice:";

    private static final AtomicInteger CLASS_COUNTER = new AtomicInteger(1);
    private final DefaultListableBeanFactory registry;
    private final Map<Class, Map<String, Pair<AbstractConfig, BeanDefinition>>> configMap = getConfigListMap();
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> applications = configMap.get(ApplicationConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> consumers = configMap.get(ConsumerConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> references = configMap.get(ReferenceConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> monitors = configMap.get(MonitorConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> modules = configMap.get(ModuleConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> registrys = configMap.get(RegistryConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> protocols = configMap.get(ProtocolConfig.class);
    private final Map<String, Pair<AbstractConfig, BeanDefinition>> providers = configMap.get(ProviderConfig.class);
    private final ApplicationConfig defaultApplicationConfig;
    private final ProviderConfig defaultProviderConfig;
    private final ConsumerConfig defaultConsumerConfig;
    private final MonitorConfig defaultMonitorConfig;
    private final List<RegistryConfig> defaultAllRegistry;
    private final List<ProtocolConfig> defaultProtocols;
    private final ModuleConfig defaultModuleConfig;

    /**
     * @param dubboBeanConfigs dubbo的配置文件
     */
    public DubboRPCModule(String... dubboBeanConfigs) {
        Preconditions.checkNotNull(dubboBeanConfigs);

        registry = new GuiceDefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        DefaultNamespaceHandlerResolver resolver = new DefaultNamespaceHandlerResolver(contextClassLoader, "spring-dubbo.handlers");
        reader.setNamespaceHandlerResolver(resolver);
        reader.loadBeanDefinitions(dubboBeanConfigs);

        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            Object bean = registry.getBean(beanName);
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            Class<?> beanClass = bean.getClass();
            if (AbstractConfig.class.isAssignableFrom(beanClass)) {
                Map<String, Pair<AbstractConfig, BeanDefinition>> pairMap = Preconditions.checkNotNull(configMap.get(beanClass), "Invalid config class %s", beanClass);
                pairMap.put(beanName, Pair.of((AbstractConfig) bean, beanDefinition));
            }
        }

        Preconditions.checkState(!protocols.isEmpty(), "No protocols");

        defaultApplicationConfig = (ApplicationConfig) getDefaultConfig(applications);
        defaultProviderConfig = (ProviderConfig) getDefaultConfig(providers);
        defaultConsumerConfig = (ConsumerConfig) getDefaultConfig(consumers);
        defaultMonitorConfig = (MonitorConfig) getDefaultConfig(monitors);
        defaultModuleConfig = (ModuleConfig) getDefaultConfig(modules);
        defaultAllRegistry = getAllDefaultConfig(registrys);
        defaultProtocols = getAllDefaultConfig(protocols);
    }

    @Override
    protected void configure() {
        //配置Service
        Map<String, Pair<AbstractConfig, BeanDefinition>> services = configMap.get(ServiceConfig.class);
        List<ServiceConfig> serviceConfigList = Lists.newArrayListWithCapacity(services.size());
        for (Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> servicePair : services.entrySet()) {
            ServiceConfig serviceConfig = setupServiceConfig(servicePair);
            serviceConfigList.add(serviceConfig);
            LOGGER.info("Add ServiceConfig {}", serviceConfig);

            //建立与Guice之间的注入关系
            Object ref = Preconditions.checkNotNull(serviceConfig.getRef(), "Must set ref for service [%s]", serviceConfig);
            Preconditions.checkState(ref instanceof ServiceGuiceRef, "The ref of service [%s] could not be supplied by spring,it should be in Guice ");
            ServiceGuiceRef serviceGuiceRef = (ServiceGuiceRef) ref;
            final Class interfaceClass = serviceConfig.getInterfaceClass();
            Key guiceBindKey;
            String interfaceClassInjectName = "";
            if (interfaceClass.getName().equals(serviceGuiceRef.getRefName())) {
                //类名绑定
                guiceBindKey = Key.get(interfaceClass);
            } else {
                //指定名称绑定
                guiceBindKey = Key.get(interfaceClass, Names.named(serviceGuiceRef.getRefName()));
                interfaceClassInjectName = serviceGuiceRef.getRefName();
            }
            requireBinding(guiceBindKey);
            String injectorHelpeName = interfaceClass.getName() + "InjectHelper" + CLASS_COUNTER.incrementAndGet();
            Class injectHelperClass = ServiceConfigInjectHelperGenerator.generateInjectHelperClass(injectorHelpeName, interfaceClass, interfaceClassInjectName);
            try {
                IServiceConfigInjectHelper setter = (IServiceConfigInjectHelper) injectHelperClass.newInstance();
                setter.setServiceConfig(serviceConfig);
                requestInjection(setter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (!serviceConfigList.isEmpty()) {
            DubboServiceExport dubboServiceExport = new DubboServiceExport(serviceConfigList);
            requestInjection(dubboServiceExport);
        }

        //配置Reference
        List<ReferenceConfig> referenceConfigList = Lists.newArrayListWithCapacity(references.size());
        for (Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> referencePair : references.entrySet()) {
            ReferenceConfig referenceConfig = setupReferenceConfig(referencePair);
            referenceConfigList.add(referenceConfig);
            LOGGER.info("Add ReferenceConfig {}", referenceConfig);

            //建立与Guice之间的绑定关系
            String id = referenceConfig.getId();
            Class interfaceClass = referenceConfig.getInterfaceClass();
            Key guiceBindKey;
            if (interfaceClass.getName().equals(id)) {
                guiceBindKey = Key.get(interfaceClass);
            } else {
                guiceBindKey = Key.get(interfaceClass, Names.named(id));
            }
            bind(guiceBindKey).toInstance(referenceConfig.get());
        }
    }

    /**
     * 配置ServiceConfig
     *
     * @param servicePair
     * @return
     * @see {@link com.alibaba.dubbo.config.spring.ServiceBean#afterPropertiesSet()}
     */
    private ServiceConfig setupServiceConfig(Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> servicePair) {
        String name = servicePair.getKey();
        Pair<AbstractConfig, BeanDefinition> value = servicePair.getValue();
        final ServiceConfig serviceConfig = (ServiceConfig) value.first;

        LOGGER.info("Config service name:{}", name);
        //设置Provider
        if (serviceConfig.getProvider() == null) {
            if (defaultProviderConfig != null) {
                LOGGER.info("Set service [{}] provider to {}", name, defaultProviderConfig);
                serviceConfig.setProvider(defaultProviderConfig);
            }
        }
        final ProviderConfig provider = serviceConfig.getProvider();

        //设置Application
        if (serviceConfig.getApplication() == null && (provider == null || provider.getApplication() == null)) {
            if (defaultApplicationConfig != null) {
                serviceConfig.setApplication(defaultApplicationConfig);
            }
        }
        final ApplicationConfig application = serviceConfig.getApplication();

        //设置Module
        if (serviceConfig.getModule() == null && (provider == null || provider.getModule() == null)) {
            if (defaultModuleConfig != null) {
                serviceConfig.setModule(defaultModuleConfig);
            }
        }

        //设置registry
        List<RegistryConfig> registries = serviceConfig.getRegistries();
        if ((isListEmpty(registries))
                && (provider == null || isListEmpty(provider.getRegistries())
                && (application == null || isListEmpty(application.getRegistries())))) {
            if (!isListEmpty(defaultAllRegistry)) {
                serviceConfig.setRegistries(defaultAllRegistry);
            }
        }

        //设置Monitor
        if (serviceConfig.getMonitor() == null
                && (provider == null || provider.getMonitor() == null)
                && (application == null || application.getMonitor() == null)) {
            if (defaultMonitorConfig != null) {
                serviceConfig.setMonitor(defaultMonitorConfig);
            }
        }

        //设置protocols
        if (isListEmpty(serviceConfig.getProtocols())
                && (provider == null || (isListEmpty(provider.getProtocols())))) {
            if (!isListEmpty(defaultProtocols)) {
                serviceConfig.setProtocols(defaultProtocols);
            }
        }

        //设置Path
        if (Strings.isNullOrEmpty(serviceConfig.getPath())) {
            if (!Strings.isNullOrEmpty(name)
                    && !Strings.isNullOrEmpty(serviceConfig.getInterface())
                    && name.startsWith(serviceConfig.getInterface())) {
                serviceConfig.setPath(name);
            }
        }

        return serviceConfig;
    }

    /**
     * 配置ReferenceConfig
     *
     * @param referencePair
     * @return
     * @see {@link com.alibaba.dubbo.config.spring.ReferenceBean#afterPropertiesSet()}
     */
    private ReferenceConfig setupReferenceConfig(Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> referencePair) {
        String name = referencePair.getKey();
        Pair<AbstractConfig, BeanDefinition> value = referencePair.getValue();
        final ReferenceConfig referenceConfig = (ReferenceConfig) value.first;
        LOGGER.info("Config reference name:{}", name);

        //设置Consumber
        if (referenceConfig.getConsumer() == null) {
            if (defaultConsumerConfig != null) {
                referenceConfig.setConsumer(defaultConsumerConfig);
            }
        }
        final ConsumerConfig consumer = referenceConfig.getConsumer();

        //设置Application
        if (referenceConfig.getApplication() == null
                && (consumer == null || consumer.getApplication() == null)) {
            if (defaultApplicationConfig != null) {
                referenceConfig.setApplication(defaultApplicationConfig);
            }
        }
        final ApplicationConfig application = referenceConfig.getApplication();

        //设置Module
        if (referenceConfig.getModule() == null
                && (consumer == null || consumer.getModule() == null)) {
            if (defaultModuleConfig != null) {
                referenceConfig.setModule(defaultModuleConfig);
            }
        }

        //默认设置所有的registry
        if (isListEmpty(referenceConfig.getRegistries())
                && (consumer == null || isListEmpty(consumer.getRegistries()))
                && (application == null || isListEmpty(application.getRegistries()))) {
            if (!isListEmpty(defaultAllRegistry)) {
                referenceConfig.setRegistries(defaultAllRegistry);
            }
        }

        //设置Monitor
        if (referenceConfig.getMonitor() == null
                && (consumer == null || consumer.getMonitor() == null)
                && (application == null || application.getMonitor() == null)) {
            if (defaultMonitorConfig != null) {
                referenceConfig.setMonitor(defaultMonitorConfig);
            }
        }
        return referenceConfig;
    }

    private void addConfig(Map<Class, Map<String, Pair<AbstractConfig, BeanDefinition>>> map, Class clazz) {
        Map<String, Pair<AbstractConfig, BeanDefinition>> config = Maps.newLinkedHashMap();
        map.put(clazz, config);
    }

    /**
     * @param map
     * @return
     */
    Object getDefaultConfig(Map<String, Pair<AbstractConfig, BeanDefinition>> map) {
        if (map.isEmpty()) {
            return null;
        }
        Object defaultConfig = null;
        Iterator<Map.Entry<String, Pair<AbstractConfig, BeanDefinition>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> next = iterator.next();
            Object config = next.getValue().first;
            if (config instanceof ApplicationConfig) {
                ApplicationConfig applicationConfig = (ApplicationConfig) config;
                defaultConfig = checkDefault(applicationConfig.isDefault(), applicationConfig, defaultConfig);
            } else if (config instanceof ProviderConfig) {
                ProviderConfig providerConfig = (ProviderConfig) config;
                defaultConfig = checkDefault(providerConfig.isDefault(), providerConfig, defaultConfig);
            } else if (config instanceof MonitorConfig) {
                MonitorConfig monitorConfig = (MonitorConfig) config;
                defaultConfig = checkDefault(monitorConfig.isDefault(), monitorConfig, defaultConfig);
            } else if (config instanceof ModuleConfig) {
                ModuleConfig moduleConfig = (ModuleConfig) config;
                defaultConfig = checkDefault(moduleConfig.isDefault(), moduleConfig, defaultConfig);
            } else if (config instanceof ConsumerConfig) {
                ConsumerConfig consumerConfig = (ConsumerConfig) config;
                defaultConfig = checkDefault(consumerConfig.isDefault(), consumerConfig, defaultConfig);
            } else {
                throw new UnsupportedOperationException("Unknown " + config.getClass());
            }
        }
        return defaultConfig;
    }

    private Object checkDefault(Boolean isDefault, Object toCheck, Object existed) {
        if (isDefault == null || isDefault) {
            Preconditions.checkState(existed == null, "Duplicate config {} and  {}", toCheck, existed);
            return toCheck;
        }
        return null;
    }

    private boolean isListEmpty(List list) {
        return list == null || list.isEmpty();
    }

    /**
     * @param map
     * @param <T>
     * @return
     */
    <T> List<T> getAllDefaultConfig(Map<String, Pair<AbstractConfig, BeanDefinition>> map) {
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = Lists.newArrayListWithCapacity(map.size());
        for (Map.Entry<String, Pair<AbstractConfig, BeanDefinition>> configPair : map.entrySet()) {
            AbstractConfig config = configPair.getValue().first;
            if (config instanceof ProtocolConfig) {
                ProtocolConfig protocolConfig = (ProtocolConfig) config;
                ProtocolConfig toAdd = (ProtocolConfig) checkDefault(protocolConfig.isDefault(), protocolConfig, null);
                if (toAdd != null) {
                    result.add((T) toAdd);
                }
            } else if (config instanceof RegistryConfig) {
                RegistryConfig registryConfig = (RegistryConfig) config;
                RegistryConfig toAdd = (RegistryConfig) checkDefault(registryConfig.isDefault(), registryConfig, null);
                if (toAdd != null) {
                    result.add((T) toAdd);
                }
            } else {
                throw new UnsupportedOperationException("Unknown " + config.getClass());
            }
        }
        return result;
    }

    private Map<Class, Map<String, Pair<AbstractConfig, BeanDefinition>>> getConfigListMap() {
        Map<Class, Map<String, Pair<AbstractConfig, BeanDefinition>>> configs = Maps.newLinkedHashMap();
        addConfig(configs, ApplicationConfig.class);
        addConfig(configs, RegistryConfig.class);
        addConfig(configs, ProtocolConfig.class);
        addConfig(configs, ProviderConfig.class);
        addConfig(configs, ServiceConfig.class);
        addConfig(configs, ConsumerConfig.class);
        addConfig(configs, ReferenceConfig.class);
        addConfig(configs, MonitorConfig.class);
        addConfig(configs, ModuleConfig.class);
        return configs;
    }


    /**
     *
     */
    private static class GuiceDefaultListableBeanFactory extends DefaultListableBeanFactory {
        @Override
        public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
            if (beanName.startsWith(SERVICE_CONFIG_REF_GUICE)) {
                List<String> strings = Splitter.on(":").trimResults().splitToList(beanName);
                Preconditions.checkState(strings.size() == 2);
                GenericBeanDefinition refBeanDefinition = new GenericBeanDefinition();
                refBeanDefinition.setBeanClass(ServiceGuiceRef.class);
                ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
                constructorArgumentValues.addGenericArgumentValue(strings.get(1));
                refBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);
                return refBeanDefinition;
            }
            return super.getBeanDefinition(beanName);
        }
    }
}
