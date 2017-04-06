package com.babyfs.tk.commons.config;

import java.util.List;

/**
 * ConfigService配置的原型
 */
public class ConfigServiceConfig {
    /**
     * 通用的URI加载配置
     */
    public static final String TYPE_COMMON_URI = "common";
    /**
     * 从环境变量及系统属性中加载配置
     */
    public static final String TYPE_SYS = "sys";
    /**
     * 从ZK中加载配置
     */
    public static final String TYPE_ZK = "zk";

    /**
     * 配置的类型
     */
    private String type;
    /**
     * 配置的URI
     */
    private List<String> uris;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }
}
