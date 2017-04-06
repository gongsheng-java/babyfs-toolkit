package com.babyfs.tk.commons.name.impl.zookeeper;

/**
 * Zookeeper相关的常量
 */
public final class ZkConstants {
    /**
     * Zookeeper Server节点名称的前缀
     */
    public static final String SERVER_NODE_PREFIX = "svr_";
    /**
     * 连接Zookeeper服务器的超时时间,单位ms
     */
    public static final int CONNECTION_TIMEOUT = 10 * 1000;

    private ZkConstants() {

    }
}
