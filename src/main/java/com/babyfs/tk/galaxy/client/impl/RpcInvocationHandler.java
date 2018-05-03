package com.babyfs.tk.galaxy.client.impl;

import com.babyfs.tk.commons.JavaProxyUtil;
import com.babyfs.tk.galaxy.ServicePoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RPC InvocationHandler实现类
 * 分发代理对象的method到对应的method handler执行
 */
public class RpcInvocationHandler implements InvocationHandler {
    private final ServicePoint target;
    private final Map<Method, MethodHandler> dispatch;

    RpcInvocationHandler(ServicePoint target, Map<Method, MethodHandler> dispatch) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch for %s", dispatch);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (dispatch.containsKey(method)) {
            return dispatch.get(method).invoke(args);
        } else {
            return JavaProxyUtil.invokeMethodOfObject(proxy, method, args, new Class[]{target.getType()});
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RpcInvocationHandler) {
            RpcInvocationHandler other = (RpcInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
