
package com.babyfs.tk.galaxy.client;


import com.babyfs.tk.galaxy.codec.Decoder;
import com.babyfs.tk.galaxy.codec.Encoder;
import com.babyfs.tk.galaxy.register.LoadBalance;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * rpc client端构建的核心类
 * 采用builder的方式构建rpc的client,encoder,decoder,loadBalance
 */
public abstract class Galaxy {

    public static Builder builder() {
        return new Builder();
    }


    public static String configKey(Class targetType, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetType.getSimpleName());
        builder.append('#').append(method.getName()).append('(');
        for (Type param : method.getGenericParameterTypes()) {
            param = Types.resolve(targetType, targetType, param);
            builder.append(Types.getRawType(param).getSimpleName()).append(',');
        }
        if (method.getParameterTypes().length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.append(')').toString();
    }


    @Deprecated
    public static String configKey(Method method) {
        return configKey(method.getDeclaringClass(), method);
    }


    public abstract <T> T newInstance(Target<T> target);

    public static class Builder {

        private Encoder encoder = new Encoder.Default();
        private Decoder decoder = new Decoder.Default();
        private Client client = RpcHttpClient.http;
        private LoadBalance loadBalance = null;

        private InvocationHandlerFactory invocationHandlerFactory =
                new InvocationHandlerFactory.Default();


        public Builder encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder decoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder cient(Client client) {

            this.client = client;
            return this;
        }

        public Builder loadBalance(LoadBalance loadBalance) {
            this.loadBalance = loadBalance;
            return this;
        }

        public Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            this.invocationHandlerFactory = invocationHandlerFactory;
            return this;
        }

        public <T> T target(Class<T> apiType, String url) {
            return target(new Target.HardCodedTarget<T>(apiType, url));
        }

        public <T> T target(Target<T> target) {
            return build().newInstance(target);
        }

        public Galaxy build() {
            SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
                    new SynchronousMethodHandler.Factory();
            ReflectiveGalaxy.ParseHandlersByName handlersByName =
                    new ReflectiveGalaxy.ParseHandlersByName(encoder, decoder, client,
                            synchronousMethodHandlerFactory, loadBalance);
            return new ReflectiveGalaxy(handlersByName, invocationHandlerFactory);
        }
    }

}
