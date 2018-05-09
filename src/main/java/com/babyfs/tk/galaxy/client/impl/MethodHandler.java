package com.babyfs.tk.galaxy.client.impl;


import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.ServiceServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 被代理对象方法的实际执行handler
 */
public final class MethodHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandler.class);
    private final ServicePoint<?> target;
    private final ICodec codec;
    private final MethodMeta metadata;
    private final IClient client;
    private final ILoadBalance loadBalance;
    private final String urlPrefix;

    public MethodHandler(ServicePoint<?> target, ICodec codec, IClient client, MethodMeta metadata, ILoadBalance loadBalance, String urlPrefix) {
        this.target = checkNotNull(target, "target");
        this.codec = checkNotNull(codec, "codec");
        this.metadata = checkNotNull(metadata, "metadata");
        this.client = checkNotNull(client, "client");
        this.loadBalance = checkNotNull(loadBalance, "loadBalance");
        this.urlPrefix = checkNotNull(urlPrefix, "urlPrefix");

    }

    /**
     * rpc 代理对象的方法实际执行方法
     * 执行编码，远程调用，解码
     *
     * @param argv
     * @return
     * @throws Throwable
     */
    public Object invoke(Object[] argv) {
        byte[] body = codec.encode(createRequest(argv));
        String interfaceName = target.getType().getName();
        ServiceServer serviceServer = loadBalance.findServer(interfaceName);
        if (serviceServer == null) {
            LOGGER.error("no serviceInstance for {}", interfaceName);
            throw new RpcException("no serviceInstance for:" + interfaceName);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceServer.getHost()).append(":").append(serviceServer.getPort());
        String url = stringBuilder.append(urlPrefix).toString();
        try {
            byte[] content = client.execute(url, body);
            return codec.decode(content);
        } catch (Exception e) {
            LOGGER.error("rpc invoke remote method fail", e);
            throw new RpcException("rpc invoke remote method fail", e);
        }
    }

    /**
     * 根据方法传入的实际参数，与方法元数据pojo构造RpcRequest
     *
     * @param argv
     * @return
     */
    private RpcRequest createRequest(Object[] argv) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(argv);
        rpcRequest.setMethodSign(metadata.getSig());
        rpcRequest.setInterfaceName(target.getInterfaceName());
        return rpcRequest;
    }
}
