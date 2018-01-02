package com.babyfs.tk.galaxy.server;

/**
 * rpc server端接口
 */
public interface IRpcService {
    /**
     * 根据rpc客户端传递的参数在服务端执行该方法
     *
     * @param className
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     */
    public Object invoke(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters);
}
