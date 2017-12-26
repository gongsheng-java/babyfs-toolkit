package com.babyfs.tk.galaxy.client;

import java.io.IOException;


/**
 * client interface
 * 执行rpc的远程调用的接口
 */
public interface Client {

    public byte[] execute(String uri, byte[] body) throws IOException;
}
