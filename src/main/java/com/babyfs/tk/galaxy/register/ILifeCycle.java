package com.babyfs.tk.galaxy.register;

import org.apache.zookeeper.KeeperException;

import java.util.concurrent.ExecutionException;

/**
 * DiscoveryClient生命周期接口
 */
public interface ILifeCycle {

    /**
     * 将本服务注册到注册中心
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void register() throws ExecutionException, InterruptedException, KeeperException;

    /**
     * 监听注册中心的节点变化
     * @throws Exception
     */
    public void watch() throws Exception;


    public void start();
}
