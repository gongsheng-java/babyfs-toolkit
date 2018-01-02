
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;
import com.babyfs.tk.galaxy.register.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * GalaxyMethodHandler
 * 被代理对象方法的实际执行handler
 * 提供了工厂类实现
 */
final class GalaxyMethodHandler implements IInvocationHandlerFactory.IMethodHandler {

    private static final Logger logger = LoggerFactory.getLogger(GalaxyMethodHandler.class);
    private final ITarget<?> target;
    private final Decoder decoder;
    private final Encoder encoder;
    private final MethodMetadata metadata;
    private final IClient client;
    private final LoadBalance loadBalance;

    private GalaxyMethodHandler(ITarget<?> target, Encoder encoder,
                                Decoder decoder, IClient client, MethodMetadata metadata, LoadBalance loadBalance) {
        this.target = checkNotNull(target, "target");
        this.decoder = checkNotNull(decoder, "decoder for %s", target);
        this.metadata = metadata;
        this.encoder = encoder;
        this.client = client;
        this.loadBalance = loadBalance;
    }
    /**
     * 执行编码，远程调用，解码
     *
     * @param argv
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object[] argv) throws Throwable {

        byte[] body = encoder.encode(createRequest(argv), RpcRequest.class);
        StringBuilder stringBuilder = new StringBuilder();
        if (loadBalance != null) {
            ServiceInstance serviceInstance = loadBalance.getServerByAppName(target.name());
            stringBuilder.append("http://").append(serviceInstance.getHost()).append(":").append(serviceInstance.getPort());
        } else {
            stringBuilder.append(target.url());
        }
        // TODO: 2018/1/2 此处需要卸载配置文件里
        String url = stringBuilder.append("/rpc/invoke").toString();
        try {
            byte[] content = client.execute(url, body);
            return decoder.decode(content);
        } catch (Exception e) {
            logger.error("rpc invoke remote method fail", e);
            return ServiceResponse.failResponse();
        }
    }

    /**
     * 根据方法传入的实际参数，与方法元数据pojo构造RpcRequest
     *
     * @param argv
     * @return
     */
    public RpcRequest createRequest(Object[] argv) {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(argv);
        rpcRequest.setMethodSign(metadata.configKey());
        rpcRequest.setInterfaceName(target.type().getName());
        rpcRequest.setMethodName(metadata.methodName());
        return rpcRequest;
    }

    /**
     * MethodHandler工厂类
     * 创建GalaxyMethodHandler对象
     */
    static class Factory {
        /**
         * create方法创建GalaxyMethodHandler
         *
         * @param target
         * @param encoder
         * @param decoder
         * @param client
         * @param md
         * @param loadBalance
         * @return
         */
        public IInvocationHandlerFactory.IMethodHandler create(ITarget<?> target, Encoder encoder, Decoder decoder, IClient client, MethodMetadata md, LoadBalance loadBalance) {
            return new GalaxyMethodHandler(target, encoder, decoder, client, md, loadBalance);
        }
    }
}
