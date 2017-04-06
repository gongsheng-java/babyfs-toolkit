package com.babyfs.tk.service.basic.probe;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 *
 */
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    /**
     * JVM 系统属性: Server的名称,-D{@value #PROP_SERVER_NAME}
     */
    static final String PROP_SERVER_NAME = "server_name";
    /**
     * JVM系统属性:是否生效,默认为生效 -D{@value  #PROP_ENABLE}=false
     */
    static final String PROP_ENABLE = "probe.enable";

    /**
     * 服务器名
     */
    private final String serverName;
    /**
     * 服务器Host :
     */
    private final String serverHost;
    /**
     * 是否生效,默认为true,即生效
     */
    private final boolean enable;

    /**
     * @param serverName
     * @param serverHost
     * @param enable
     */
    public Config(String serverName, String serverHost, boolean enable) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.serverHost = Preconditions.checkNotNull(serverHost);
        this.enable = enable;
    }

    /**
     * 当没有设置属性的时候,尝试从系统属性中取得配置信息
     * 1. 从系统属性{@value #PROP_SERVER_NAME} 取得 {@link #serverName}
     * 2. 从系统属性{@value #PROP_ENABLE} 取得是否禁用
     * 3. 自动识别内网的ip,作为serverHost
     */
    public static Config getDefaultConfig() {
        boolean enable = getEnableFromSysProp();
        InetAddress localHostAddress = getLocalHostAddress();
        String serverHost = localHostAddress != null ? localHostAddress.getHostName() : "";
        String serverName = getServerNameFromSysProp();
        return new Config(serverName, serverHost, enable);
    }


    public String getServerName() {
        return serverName;
    }

    public String getServerHost() {
        return serverHost;
    }

    public boolean isEnable() {
        return enable;
    }

    /**
     * @return
     */
    public static String getServerNameFromSysProp() {
        String serverName = System.getProperty(PROP_SERVER_NAME);
        if (serverName == null) {
            serverName = "Unknown";
        }
        return serverName;
    }

    /**
     * 取得本机的地址,依据如下的规则获取地址:
     * <ul>
     * <li>如果本机有内网地址,即(10.x或者192.168.x),则返回第一个找到地址</li>
     * <li>如果没有内网地址,则返回{@link InetAddress#getLocalHost()}</li>
     * </ul>
     *
     * @return 在抛出异常的情况下, 返回null
     */
    public static InetAddress getLocalHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Can't get the local host address", e);
        }
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.error("Can't get the lo  host address", e);
        }
        return null;
    }

    /**
     * @return
     */
    public static boolean getEnableFromSysProp() {
        return !(Boolean.getBoolean(PROP_ENABLE));
    }
}
