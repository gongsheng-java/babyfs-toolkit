package com.babyfs.tk.galaxy.server.impl;


import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.proxy.ReflectionUtils;
import com.babyfs.tk.galaxy.server.IRpcService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RpcServiceImpl implements IRpcService {


    private Injector injector;

    Logger logger = LoggerFactory.getLogger("RpcServiceImpl");

    @Inject
    public RpcServiceImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Object invoke(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws ClassNotFoundException, NoSuchMethodException {

        if (Strings.isNullOrEmpty(className) || Strings.isNullOrEmpty(methodName) || parameters == null || parameterTypes == null) {
            logger.error("error para,className:{},methodName:{},parameterType:{},parameters", className, methodName, parameterTypes, parameters);
            throw new RpcException("error parameter");
        }
        Class<?> bean = Class.forName(className);
        Object object = injector.getInstance(bean);
        Method method = bean.getMethod(methodName, parameterTypes);
        Object returnObj = ReflectionUtils.invokeMethod(method, object, parameters);
        return returnObj;
    }
}
