
package com.babyfs.tk.galaxy.client;


import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * 记录方法元数据的pojo
 */
public final class MethodMetadata implements Serializable {

    private static final long serialVersionUID = 1L;
    private String configKey;
    private transient Type returnType;
    private String methodName;
    private Class<?>[] parameterTypes;

    MethodMetadata() {
    }


    public String configKey() {
        return configKey;
    }

    public MethodMetadata configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public MethodMetadata methodName(String methodName) {

        this.methodName = methodName;
        return this;
    }

    public MethodMetadata parameterTypes(Class<?>[] parameterTypes) {

        this.parameterTypes = parameterTypes;
        return this;
    }

    public String methodName() {

        return methodName;
    }

    public Class<?>[] parameterTypes() {

        return parameterTypes;
    }

    public Type returnType() {
        return returnType;
    }

    public MethodMetadata returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }


}
