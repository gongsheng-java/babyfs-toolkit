
package com.babyfs.tk.galaxy.client;


import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * 记录方法元数据的pojo
 */
public final class MethodMetadata implements Serializable {

    private static final long serialVersionUID = 1L;
    //方法签名
    private String configKey;


    MethodMetadata() {
    }

    public MethodMetadata configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public String configKey() {
        return configKey;
    }

}
