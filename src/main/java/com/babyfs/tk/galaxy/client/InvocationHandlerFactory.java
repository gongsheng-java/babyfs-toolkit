package com.babyfs.tk.galaxy.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;


public interface InvocationHandlerFactory {

    InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch);

    interface MethodHandler {

        Object invoke(Object[] argv) throws Throwable;
    }

    static final class Default implements InvocationHandlerFactory {

        @Override
        public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
            return new ReflectiveGalaxy.RpcInvocationHandler(target, dispatch);
        }
    }
}
