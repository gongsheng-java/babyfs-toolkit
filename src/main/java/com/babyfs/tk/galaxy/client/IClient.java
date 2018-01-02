package com.babyfs.tk.galaxy.client;


/**
 * client interface
 * 执行client与server间远程调用的接口
 * 是rpc框架的传输层
 */
public interface IClient {

    /**
     * 执行rpc远程调用的方法
     * @param uri rpc调用的目标地址
     * @param body rpc调用client端发送的body,为二进制格式
     * @return rpc调用返回的response，二进制格式
     */
    byte[] execute(String uri, byte[] body) ;
}
