package com.babyfs.tk.galaxy;

import java.io.Serializable;
import java.util.Arrays;

/**
 * rpc调用过程客户端传递到服务端的pojo对象
 */
public class RpcRequest implements Serializable {

    private String interfaceName;
    private String methodName;
    private String methodSign;

    private Object[] parameters;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public void setMethodSign(String methodSign) {
        this.methodSign = methodSign;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodSign='" + methodSign + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }


}