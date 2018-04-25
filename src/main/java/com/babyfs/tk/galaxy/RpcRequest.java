package com.babyfs.tk.galaxy;

import java.io.Serializable;
import java.util.Arrays;

/**
 * rpc调用过程客户端传递到服务端的pojo对象
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 2003969790677366364L;
    //接口名称
    private String interfaceName;
    //方法签名
    private String methodSign;
    //方法参数
    private Object[] parameters;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
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
                ", methodSign='" + methodSign + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}