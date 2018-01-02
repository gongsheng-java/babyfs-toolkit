package com.babyfs.tk.galaxy.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;


/**
 * InvocationHandler工厂类，create方法为创建InvocationHandler的方法
 * 本类中提供了IInvocationHandlerFactory的默认实现创建RpcInvocationHandler
 */
public interface IInvocationHandlerFactory {

    /**
     * 创建InvocationHandler方法
     * @param target 代理的目标
     * @param dispatch key为Method，value为MethodHandler的map
     * @return InvocationHandler
     */
    InvocationHandler create(ITarget target, Map<Method, IMethodHandler> dispatch);

    /**
     * MethodHandler接口,此接口的主要目标为用于代理对象的方法的实际执行过程
     * 其中代理方法的执行为invoke方法
     */
    interface IMethodHandler {

        /**
         * 执行被代理对象的方法实际执行过程
         * @param argv 方法实际传入的参数
         * @return 方法执行结果
         * @throws Throwable
         */
        Object invoke(Object[] argv) throws Throwable;
    }

    /**
     * handler工厂类的默认实现，create方法的返回值为,RpcInvocationHandler
     */
    final class Default implements IInvocationHandlerFactory {

        @Override
        public InvocationHandler create(ITarget target, Map<Method, IMethodHandler> dispatch) {
            return new ReflectiveGalaxyClient.RpcInvocationHandler(target, dispatch);
        }
    }
}
