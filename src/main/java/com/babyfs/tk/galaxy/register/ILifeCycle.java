package com.babyfs.tk.galaxy.register;

import org.apache.zookeeper.KeeperException;

import java.util.concurrent.ExecutionException;

public interface ILifeCycle {

    public void register() throws ExecutionException, InterruptedException, KeeperException;

    public void deRegister() throws KeeperException, InterruptedException;

    public void watch() throws Exception;
}
