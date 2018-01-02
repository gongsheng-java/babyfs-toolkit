package com.babyfs.tk.galaxy.server.impl;


import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.server.IMethodCacheService;
import com.babyfs.tk.galaxy.server.IRpcService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * rpc server接口实现
 */
public class RpcServiceImpl implements IRpcService {


    private Injector injector;

    private IMethodCacheService methodCacheService;

    Logger logger = LoggerFactory.getLogger("RpcServiceImpl");

    @Inject
    public RpcServiceImpl(Injector injector, IMethodCacheService methodCacheService) {
        this.injector = injector;
        this.methodCacheService = methodCacheService;
    }

    @Override
    public Object invoke(String className, String methodName, String methodSign, Object[] parameters) {

        if (Strings.isNullOrEmpty(className) || Strings.isNullOrEmpty(methodName) || parameters == null || Strings.isNullOrEmpty(methodSign)) {
            logger.error("error para,className:{},methodName:{},parameterType:{},parameters", className, methodName, methodSign, parameters);
            throw new RpcException("error parameter");
        }
        try {
            Class<?> bean = Class.forName(className);
            Object object = injector.getInstance(bean);
            Method method = methodCacheService.getMethodBySign(methodSign);
            Object obj = method.invoke(object, parameters);
            return obj;
        } catch (Exception e) {
            logger.error("rpc server invoke error", e);
            throw new RpcException("rpc server invoke error", e);
        }
    }
}
