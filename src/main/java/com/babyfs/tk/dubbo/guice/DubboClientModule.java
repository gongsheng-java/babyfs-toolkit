package com.babyfs.tk.dubbo.guice;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.dal.guice.DalXmlConfModule;
import com.babyfs.tk.dubbo.xml.DubboClients;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class DubboClientModule extends ServiceModule {

    private static final Logger logger = LoggerFactory.getLogger(DalXmlConfModule.class);

    static {
        initEnv();
    }

    private boolean hasLoadConfig = false;

    private String application, xmlName;

    private List<DubboClients.DubboClient> dubboClients;

    public DubboClientModule(String applicationName, String xmlName) {
        this.application = applicationName;
        this.xmlName = xmlName;
        this.loadConfig();
    }

    private static void initEnv(){
        System.setProperty("dubbo.application.logger", "slf4j");

    }

    @Override
    protected void configure() {
        ApplicationConfig applicationConfig = new ApplicationConfig(application);

        if (hasLoadConfig) {
            for (DubboClients.DubboClient client :
                    dubboClients) {
                bindClient(client, applicationConfig);
            }
        }
    }

    private void bindClient(DubboClients.DubboClient client, ApplicationConfig applicationConfig) {

        Class tClass = null;
        try {
            tClass = Class.forName(client.getType());
        } catch (Exception e) {
            logger.warn("find class {} fail, cannot create dubbo client", client.getType());
            return;
        }

        if (hasRegistry(client.getRegistry())) {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(client.getRegistry());
            registryConfig.setCheck(false);
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setInterface(tClass);
            reference.setVersion(client.getVersion());
            reference.setCheck(false);//设置懒加载
            reference.setTimeout(30000);
            Object ref = reference.get();
            bindService(tClass, ref);
            logger.info("create dubbo client {} by registry {} succeed!", client.getType(), client.getRegistry());
        } else {
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            String url = String.format("dubbo://%s/%s", client.getUrl(), client.getType());
            reference.setUrl(url);
            reference.setTimeout(30000);
            reference.setInterface(tClass);
            reference.setApplication(applicationConfig);
            reference.setVersion(client.getVersion());
            reference.setCheck(false);//设置懒加载
            Object ref = reference.get();
            bindService(tClass, ref);
            logger.info("create dubbo client {} by url {} succeed!", client.getType(), url);
        }
    }

    private boolean hasRegistry(String registry) {
        return StringUtils.isNotEmpty(registry) && !"N/A".equals(registry);
    }

    private void loadConfig() {
        try {
            DubboClients loadClients = loadDubboConfig(DubboClients.class, xmlName);
            if (loadClients == null || CollectionUtils.isEmpty(loadClients.getDubboClients())) {
                logger.error("error in load dubbo config, load config fail");
            }
            this.dubboClients = loadClients.getDubboClients();
            hasLoadConfig = true;

        } catch (Exception e) {
            logger.error("error in load dubbo config", e);
        }
    }

    /**
     * 加载DubboConfig
     *
     * @param clazz
     * @param confPath
     * @param <T>
     * @return
     */
    protected static <T> T loadDubboConfig(Class<T> clazz, String confPath) {
        logger.info("load dubbo config {} from classpath", confPath);
        return JAXBUtil.unmarshal(clazz, confPath);
    }

}
