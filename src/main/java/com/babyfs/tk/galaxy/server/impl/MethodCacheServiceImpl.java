package com.babyfs.tk.galaxy.server.impl;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.ServiceEnrty;
import com.babyfs.tk.galaxy.ProxyUtils;
import com.babyfs.tk.galaxy.server.IMethodCacheService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.babyfs.tk.galaxy.ProxyUtils.FORBIDDEN_METHODS;


/**
 * 方法缓存Service实现
 */
public class MethodCacheServiceImpl implements IMethodCacheService {

    // hashMap key:方法签名，value:方法
    private final Map<String, Method> methodMap = new ConcurrentHashMap<>();

    private Injector injector;

    @Inject
    public MethodCacheServiceImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ServiceResponse<Method> getMethodBySign(String sign) {
        if (!methodMap.containsKey(sign)) {
            return ServiceResponse.createFailResponse("invalid method sign:" + sign);
        }
        return ServiceResponse.createSuccessResponse(methodMap.get(sign));
    }

    //guice容器加载的时候执行的methodCache初始化方法
    public void init() {
        Set<ServiceEnrty> allServices = ServiceEnrty.getAllServices(injector);
        for (ServiceEnrty entry : allServices) {
            Key<?> key = entry.getGuiceKey();
            Object obj = injector.getInstance(key);
            Method[] methods = obj.getClass().getMethods();
            for (Method method : methods) {
                if (FORBIDDEN_METHODS.contains(method)) {
                    continue;
                }
                String methodSign = ProxyUtils.configKey(entry.getName(), method);
                methodMap.put(methodSign, method);
            }
        }
    }
}