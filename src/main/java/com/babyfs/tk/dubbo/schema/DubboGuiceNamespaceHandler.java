package com.babyfs.tk.dubbo.schema;

import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.config.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 *
 */
public class DubboGuiceNamespaceHandler extends NamespaceHandlerSupport {
    static {
        Version.checkDuplicate(DubboGuiceNamespaceHandler.class);
    }

    public void init() {
        registerBeanDefinitionParser("application", new DubboGuiceBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("module", new DubboGuiceBeanDefinitionParser(ModuleConfig.class, true));
        registerBeanDefinitionParser("registry", new DubboGuiceBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("monitor", new DubboGuiceBeanDefinitionParser(MonitorConfig.class, true));
        registerBeanDefinitionParser("provider", new DubboGuiceBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new DubboGuiceBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new DubboGuiceBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new DubboGuiceBeanDefinitionParser(ServiceConfig.class, true));
        registerBeanDefinitionParser("reference", new DubboGuiceBeanDefinitionParser(ReferenceConfig.class, false));
    }
}
