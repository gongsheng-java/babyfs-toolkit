package com.babyfs.tk.galaxy.server;

import java.lang.reflect.Method;

/**
 * 方法缓存服务
 */
public interface IMethodCacheService {

    /**
     * 根据方法签名获取方法
     * @param sign
     * @return
     */
    Method getMethodBySign(String sign);

    /**
     * 初始化方法
     */
    void init();
}
