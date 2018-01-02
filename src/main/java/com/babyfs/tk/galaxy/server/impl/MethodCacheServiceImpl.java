package com.babyfs.tk.galaxy.server.impl;

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

public class MethodCacheServiceImpl implements IMethodCacheService {

    private final Map<String, Method> methodMap = new HashMap();

    private Injector injector;

    @Inject
    public MethodCacheServiceImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Method getMethodBySign(String sign) {
        if (methodMap.containsKey(sign)) {
            return methodMap.get(sign);
        }
        return null;
    }

    public void init() {
        Set<ServiceEnrty> allServices = ServiceEnrty.getAllServices(injector);
        for (ServiceEnrty entry : allServices) {
            Key<?> key = entry.getGuiceKey();
            Object obj = injector.getInstance(key);
            Method[] methods = obj.getClass().getMethods();
            for (Method method : methods) {
                String methodSign = ProxyUtils.configKey(obj.getClass(), method);
                methodMap.put(methodSign, method);
            }
        }
    }
}