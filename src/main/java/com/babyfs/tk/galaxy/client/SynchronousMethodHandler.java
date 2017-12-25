
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;
import com.babyfs.tk.galaxy.register.ServiceInstance;

final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {

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

    @Override
    public Object invoke(Object[] argv) throws Throwable {

        byte[] body = encoder.encode(createRequest(argv), RpcRequest.class);
        String url;
        String path = "/rpc/invoke";
        if (loadBalance != null) {
            ServiceInstance serviceInstance = loadBalance.getServerByAppName(target.name());
            url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + path;
        } else {
            url = target.url() + path;
        }
        byte[] content = client.execute(url, body);
        return decoder.decode(content, metadata.returnType());
    }

    public RpcRequest createRequest(Object[] argv) {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(argv);
        rpcRequest.setParameterTypes(metadata.parameterTypes());
        rpcRequest.setClassName(target.type().getName());
        rpcRequest.setMethodName(metadata.methodName());
        return rpcRequest;
    }

    static class Factory {

        public InvocationHandlerFactory.MethodHandler create(Target<?> target, Encoder encoder, Decoder decoder, Client client, MethodMetadata md, LoadBalance loadBalance) {
            return new SynchronousMethodHandler(target, encoder, decoder, client, md, loadBalance);
        }
    }
}
