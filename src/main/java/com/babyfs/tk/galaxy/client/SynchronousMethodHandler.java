
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;

final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {

    private final Target<?> target;
    private final Decoder decoder;
    private final Encoder encoder;
    private final MethodMetadata metadata;
    private final Client client;

    private SynchronousMethodHandler(Target<?> target, Encoder encoder,
                                     Decoder decoder, Client client,MethodMetadata metadata) {
        this.target = Util.checkNotNull(target, "target");
        this.decoder = Util.checkNotNull(decoder, "decoder for %s", target);
        this.metadata = metadata;
        this.encoder = encoder;
        this.client = client;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {

        String body = encoder.encode(createRequest(argv), RpcRequest.class).toString();
        String content = client.execute(target.url(), body);
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


        public InvocationHandlerFactory.MethodHandler create(Target<?> target, Encoder encoder, Decoder decoder,Client client, MethodMetadata md) {
            return new SynchronousMethodHandler(target, encoder, decoder,client,md);
        }
    }
}
