package com.babyfs.tk.galaxy.server;

public interface IRpcService {

    public Object invoke(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws ClassNotFoundException, NoSuchMethodException;
}
