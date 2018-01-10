package com.babyfs.tk.galaxy.server;

import com.babyfs.tk.commons.model.ServiceResponse;

import java.lang.reflect.Method;

/**
 * 方法缓存Service
 */
public interface IMethodCacheService {

    /**
     * 根据方法签名获取方法
     *
     * @param sign
     * @return
     */
    ServiceResponse<Method> getMethodBySign(String sign);

    /**
     * 初始化方法缓存
     */
    void init();
}
