package com.babyfs.tk.galaxy.client.impl;


import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 被代理对象方法的实际执行handler
 */
public final class MethodHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandler.class);
    private final ServicePoint<?> target;
    private final IDecoder decoder;
    private final IEncoder encoder;
    private final MethodMeta metadata;
    private final IClient client;
    private final ILoadBalance loadBalance;
    private final String urlPrefix;

    public MethodHandler(ServicePoint<?> target, IEncoder encoder, IDecoder decoder, IClient client, MethodMeta metadata, ILoadBalance loadBalance, String urlPrefix) {
        this.target = checkNotNull(target, "target for %s", target);
        this.decoder = checkNotNull(decoder, "decoder for %s", decoder);
        this.metadata = checkNotNull(metadata, "metadata for %s", metadata);
        this.encoder = checkNotNull(encoder, "encode for %s", encoder);
        this.client = checkNotNull(client, "client for %s", client);
        this.loadBalance = checkNotNull(loadBalance, "loadBalance for %s", loadBalance);
        this.urlPrefix = checkNotNull(urlPrefix, "urlPrefix for %s", urlPrefix);

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
        byte[] body = encoder.encode(createRequest(argv));
        String interfaceName = target.getType().getName();
        ServiceInstance serviceInstance = loadBalance.getServerByName(interfaceName);
        if (serviceInstance == null) {
            LOGGER.error("no serviceInstance by appName:" + interfaceName);
            throw new RpcException("no serviceInstance by appName:" + interfaceName);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceInstance.getHost()).append(":").append(serviceInstance.getPort());
        String url = stringBuilder.append(urlPrefix).toString();
        try {
            byte[] content = client.execute(url, body);
            return decoder.decode(content);
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
