package com.babyfs.tk.service.basic.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.service.basic.xml.client.ServiceGroup;
import com.babyfs.tk.service.basic.xml.server.Server;
import com.babyfs.tk.service.basic.xml.server.Servers;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础的ConfModule ： 绑定单个服务的配置文件，需要继承，暂时提供两种初始化方式
 * <p/>
 */
public abstract class BasicServiceConfModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicServiceConfModule.class);

    /**
     * 覆盖配置文件中的server配置的系统参数
     * 值的格式如下：
     * name1:host1:port1,name2:host2:port2,...
     * <p/>
     * 注：
     * 1. 目前只可覆盖host和port，其余的值如果有需求的话，可以再进行增加
     * 2. name1,name2..的格式为 {配置文件名}_{服务名}
     */
    private static final String KEY_OVERRIDE_SERVER_CONFIG = "override_server_config";

    /**
     * 最大端口号
     */
    private static final int MAX_PORT_NUMBER = 65535;

    private static final Map<String, Server> OVERRIDE_SERVICE_CONFIG = getOverrideServerConfig();

    /**
     * 将配置文件绑定到指定名字的服务上
     *
     * @param annotation
     */
    protected synchronized void bindConfByAnnotation(Class<? extends Annotation> annotation, Servers servers, ServiceGroup serviceGroup) {

        Preconditions.checkArgument(servers != null, "servers");
        Preconditions.checkArgument(!servers.getServers().isEmpty(), "Servers.servers");
        Preconditions.checkArgument(serviceGroup != null, "serviceGroup");
        bind(Servers.class).annotatedWith(annotation).toInstance(servers);
        bind(ServiceGroup.class).annotatedWith(annotation).toInstance(serviceGroup);

    }

    /**
     * 将配置文件绑定到指定名字的服务上
     *
     * @param annotation
     */
    protected synchronized void bindXmlConfByAnnotation(Class<? extends Annotation> annotation, String serversXml, String serviceGroupXml) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(serversXml), "serversXml");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceGroupXml), "serviceGroupXml");
        Servers servers = JAXBUtil.unmarshal(Servers.class, serversXml);
        if (!OVERRIDE_SERVICE_CONFIG.isEmpty()) {
            for (Server server : servers.getServers().values()) {
                String key = buildServerKey(serversXml, server.getName());
                Server s = OVERRIDE_SERVICE_CONFIG.get(key);
                if (s != null) {
                    LOGGER.info("mapping server key [{}], override to [host:{}, port:{}]", key, s.getHost(), s.getPort());
                    server.setHost(s.getHost());
                    server.setPort(s.getPort());
                }
            }
        }
        this.bindConfByAnnotation(annotation, servers, JAXBUtil.unmarshal(ServiceGroup.class, serviceGroupXml));
    }


    /**
     * 生成服务全局唯一key
     *
     * @param serversXml
     * @param serverName
     * @return
     */
    private static String buildServerKey(String serversXml, String serverName) {
        return serversXml + "_" + serverName;
    }

    /**
     * 获取系统属性里配置的覆盖信息
     *
     * @return
     */
    private static Map<String, Server> getOverrideServerConfig() {
        String serverConfigs = System.getProperty(KEY_OVERRIDE_SERVER_CONFIG);
        if (Strings.isNullOrEmpty(serverConfigs)) {
            return new HashMap<String, Server>(0);
        }
        LOGGER.info("find override server config property, values is [{}]", serverConfigs);
        String[] servers = StringUtils.split(serverConfigs, ',');
        Map<String, Server> map = new HashMap<String, Server>(servers.length);
        for (String server : servers) {
            if (Strings.isNullOrEmpty(server)) {
                continue;
            }
            String[] attr = StringUtils.split(server, ':');
            if (attr.length < 2) {
                throwErrors(serverConfigs, server);
            }
            String key = attr[0];
            // 有重复key，直接抛出异常
            if (map.containsKey(key)) {
                throwErrors(serverConfigs, "duplicate key: " + key);
            }

            String host = attr[1];
            String port = attr[2];
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(port)) {
                throwErrors(serverConfigs, server);
            }
            if (!StringUtils.isNumeric(port)) {
                throwErrors(serverConfigs, server);
            }
            int n = Integer.parseInt(port);
            if (n < 0 || n > MAX_PORT_NUMBER) {
                throwErrors(serverConfigs, server);
            }
            Server s = new Server();
            s.setHost(host);
            s.setPort(port);
            map.put(key, s);
        }
        return ImmutableMap.copyOf(map);
    }

    private static void throwErrors(String configs, String cause) {
        throw new IllegalStateException(String.format("illegal server config property [%s], cause: %s", configs, cause));
    }
}
