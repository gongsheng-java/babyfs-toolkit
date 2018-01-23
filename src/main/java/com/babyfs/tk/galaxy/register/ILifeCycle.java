package com.babyfs.tk.galaxy.register;

import org.apache.zookeeper.KeeperException;

import java.util.concurrent.ExecutionException;

/**
 * DiscoveryClient生命周期接口
 */
public interface ILifeCycle {


    /**
     * 将本节点的信息加到注册中心
     *
     * @throws Exception
     */
    void register() throws Exception;

    /**
     * 监听注册中心的节点变化
     *
     * @throws Exception
     */
    void watch() throws Exception;


    /**
     * DiscoveryClient启动方法
     */
    void start() throws Exception;
}
