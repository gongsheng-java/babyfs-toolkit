package com.babyfs.tk.galaxy.server;

import java.lang.reflect.Method;

public interface IMethodCacheService {

    Method getMethodBySign(String sign);

    void init();
}
