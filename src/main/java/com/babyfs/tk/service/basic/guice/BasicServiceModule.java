package com.babyfs.tk.service.basic.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.guice.GuiceKeys;
import com.babyfs.tk.commons.xml.XmlProperties;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceConf;
import com.babyfs.tk.service.basic.guice.annotation.ServiceQueueKestrel;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 基础的BasicServiceModule :
 * <p/>
 * 所有的BasicService通过该类进行绑定
 * <p/>
 */
public abstract class BasicServiceModule extends PrivateModule {
    /**
     * 绑定初始化一个基础命名服务
     * 该绑定需要指定{@link INameResourceService}泛性类别，如 INameResourceService<IQueue>
     * 这样可以在服务中明确返回的服务对象，如下使用:
     *
     * @param resourceType 服务的类型
     * @param provider
     * @param annotation   当前支持的注解类型：{@link ServiceQueueKestrel} 等
     */
    protected synchronized <T> void bindBasicService(Class<? extends Annotation> annotation, Class<T> resourceType, Class<? extends Provider<INameResourceService<T>>> provider) {
        Key<INameResourceService<T>> key = GuiceKeys.getKey(INameResourceService.class, annotation, resourceType);
        exposeKeyAndProvider(key, provider);
    }

    /**
     * 绑定一个带有配置的服务
     *
     * @param serviceClass 服务类型
     * @param xmlPropConf  在类路径中的配置文件
     * @param provider     服务提供者
     * @param <T>          服务的类型
     */
    protected synchronized <T> void bindServiceWithConf(Class<T> serviceClass, String xmlPropConf, Class<? extends Provider<T>> provider) {
        bindServiceWithConf0(serviceClass, null, xmlPropConf, provider);
    }

    /**
     * 绑定一个命名的带有配置的服务
     *
     * @param serviceClass 服务类型
     * @param serviceName  服务的名称
     * @param xmlPropConf  在类路径中的配置文件
     * @param provider     服务提供者
     * @param <T>          服务的类型
     */
    protected synchronized <T> void bindNamedServiceWithConf(Class<T> serviceClass, String serviceName, String xmlPropConf, Class<? extends Provider<T>> provider) {
        Preconditions.checkNotNull(serviceName, "The serviceName must not be null");
        bindServiceWithConf0(serviceClass, serviceName, xmlPropConf, provider);
    }

    /**
     * 绑定一个命名的带有配置的服务
     *
     * @param serviceClass 服务类型
     * @param serviceName  服务的名称
     * @param provider     服务提供者
     * @param conf         服务配置
     * @param <T>          服务的类型
     */
    protected synchronized <T> void bindServiceWithConf(Class<T> serviceClass, String serviceName, Class<? extends Provider<T>> provider, Map<String, String> conf) {
        Key<Object> confKey = GuiceKeys.getKey(Map.class, ServiceConf.class, String.class, String.class);
        bind(confKey).toInstance(conf);
        bindServiceWitchProvider(serviceClass, serviceName, provider);
    }

    /**
     *
     * @param serviceClass
     * @param serviceName
     * @param provider
     * @param <T>
     */
    protected synchronized <T> void bindServiceWitchProvider(Class<T> serviceClass, String serviceName, Class<? extends Provider<T>> provider) {
        Key<T> key = null;
        if (serviceName != null) {
            key = Key.get(serviceClass, Names.named(serviceName));
        } else {
            key = Key.get(serviceClass);
        }
        exposeKeyAndProvider(key, provider);
    }

    /**
     * 绑定一个带有配置的服务
     *
     * @param serviceClass 服务类型
     * @param serviceName  服务的名称
     * @param xmlPropConf  在类路径中的配置文件
     * @param provider     服务提供者
     * @param <T>          服务的类型
     */
    private synchronized <T> void bindServiceWithConf0(Class<T> serviceClass, String serviceName, String xmlPropConf, Class<? extends Provider<T>> provider) {
        Preconditions.checkNotNull(serviceClass, "The serviceClass must not be null");
        Preconditions.checkNotNull(xmlPropConf, "The serviceClass must not be null");
        Preconditions.checkNotNull(provider, "The provider must not be null");
        Map<String, String> conf = XmlProperties.loadFromXml(xmlPropConf);
        Preconditions.checkNotNull(conf, "Can't load config from %s", xmlPropConf);
        bindServiceWithConf(serviceClass, serviceName, provider, conf);
    }


    private <T> void exposeKeyAndProvider(Key<T> key, Class<? extends Provider<T>> provider) {
        bind(key).toProvider(provider).asEagerSingleton();
        expose(key);
    }
}
