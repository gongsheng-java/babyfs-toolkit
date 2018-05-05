package com.babyfs.tk.galaxy.server;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.galaxy.RpcRequest;

/**
 * rpc server端,执行实际的方法e
 */
public interface IServer extends ILifeService {
    /**
     * 处理rcp服务的请求
     *
     * @param content
     * @return
     */
    ServiceResponse<byte[]> handle(byte[] content);

    /**
     * 处理rcp服务的请求
     *
     * @param request
     * @return
     */
    ServiceResponse<Object> handle(RpcRequest request);
}
