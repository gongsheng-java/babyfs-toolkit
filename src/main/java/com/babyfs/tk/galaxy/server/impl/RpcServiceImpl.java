package com.babyfs.tk.galaxy.server.impl;


import com.babyfs.tk.commons.model.ServiceResponse;
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

    private static final Logger LOGGER = LoggerFactory.getLogger("RpcServiceImpl");

    @Inject
    public RpcServiceImpl(Injector injector, IMethodCacheService methodCacheService) {
        this.injector = injector;
        this.methodCacheService = methodCacheService;
    }

    @Override
    public ServiceResponse invoke(String interfaceName, String methodSign, Object[] parameters) {

        if (Strings.isNullOrEmpty(interfaceName) || parameters == null || Strings.isNullOrEmpty(methodSign)) {
            LOGGER.error("error para,interfaceName:{},methodSign:{},parameters：{}", interfaceName, methodSign, parameters);
            return ServiceResponse.createFailResponse("error parameters");
        }
        try {
            Class<?> bean = Class.forName(interfaceName);
            Object object = injector.getInstance(bean);
            ServiceResponse<Method> methodCacheServiceResponse = methodCacheService.getMethodBySign(methodSign);
            if (methodCacheServiceResponse.isFailure()) {
                return ServiceResponse.createFailResponse(methodCacheServiceResponse.getMsg());
            }
            Method method = methodCacheServiceResponse.getData();
            Object obj = method.invoke(object, parameters);
            return ServiceResponse.createSuccessResponse(obj);
        } catch (Exception e) {
            LOGGER.error("RpcServiceImpl@invoke error", e);
            return ServiceResponse.createFailResponse(e.getMessage());
        }

    }
}
