package com.babyfs.tk.galaxy.server.impl;


import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.codec.IDecoder;
import com.babyfs.tk.galaxy.codec.IEncoder;
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

    private IDecoder decoder;

    private IEncoder encoder;

    private static final Logger LOGGER = LoggerFactory.getLogger("RpcServiceImpl");

    @Inject
    public RpcServiceImpl(Injector injector, IMethodCacheService methodCacheService, IDecoder decoder, IEncoder encoder) {
        this.injector = injector;
        this.methodCacheService = methodCacheService;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public Object invoke(String interfaceName, String methodSign, Object[] parameters) {

        if (Strings.isNullOrEmpty(interfaceName) || Strings.isNullOrEmpty(methodSign)) {
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
            Object obj;
            if (null == parameters) {
                obj = method.invoke(object);
            } else {
                obj = method.invoke(object, parameters);
            }
            return obj;
        } catch (Exception e) {
            LOGGER.error("RpcServiceImpl@invoke error", e);
            throw new RpcException("rpc service invoke fail", e);
        }

    }

    @Override
    public byte[] invoke(byte[] content) {
        RpcRequest request = decoder.decode(content, RpcRequest.class);
        Object result = invoke(request.getInterfaceName(), request.getMethodSign(), request.getParameters());
        byte[] code = encoder.encode(result);
        return code;

    }
}
