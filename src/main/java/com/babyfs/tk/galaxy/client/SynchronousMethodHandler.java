
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;
import com.babyfs.tk.galaxy.register.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行代理类的远程方法的MethodHandler
 */
final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {

    private static final Logger logger = LoggerFactory.getLogger(SynchronousMethodHandler.class);
    private final Target<?> target;
    private final Decoder decoder;
    private final Encoder encoder;
    private final MethodMetadata metadata;
    private final Client client;
    private final LoadBalance loadBalance;

    private SynchronousMethodHandler(Target<?> target, Encoder encoder,
                                     Decoder decoder, Client client, MethodMetadata metadata, LoadBalance loadBalance) {
        this.target = Util.checkNotNull(target, "target");
        this.decoder = Util.checkNotNull(decoder, "decoder for %s", target);
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
        String url = stringBuilder.append("/rpc/invoke").toString();
        try {
            byte[] content = client.execute(url, body);
            return decoder.decode(content, metadata.returnType());
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
        rpcRequest.setParameterTypes(metadata.parameterTypes());
        rpcRequest.setClassName(target.type().getName());
        rpcRequest.setMethodName(metadata.methodName());
        return rpcRequest;
    }

    /**
     * SynchronousMethodHandler工厂类
     */
    static class Factory {

        public InvocationHandlerFactory.MethodHandler create(Target<?> target, Encoder encoder, Decoder decoder, Client client, MethodMetadata md, LoadBalance loadBalance) {
            return new SynchronousMethodHandler(target, encoder, decoder, client, md, loadBalance);
        }
    }
}
