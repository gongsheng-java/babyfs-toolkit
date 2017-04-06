package com.babyfs.tk.rpc;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC请求的封装
 */
public class Request extends RPC {

    /**
     * 用于生成id的sequence
     */
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    /**
     * service的名称
     */
    private final String serviceName;
    /**
     * service中的方法名称
     */
    private final String methodName;
    /**
     * 方法的id,用于支持支持重载
     */
    private final String methodId;
    /**
     * 请求的参数,null表示void
     */
    private final Object[] parameters;


    /**
     * @param serviceName
     * @param methodName
     * @param parameters
     */
    public Request(String serviceName, String methodName, String metodId, Object[] parameters) {
        this(serviceName, methodName, metodId, SEQ.incrementAndGet(), parameters);
    }

    /**
     * @param serviceName 服务名称,必须非空
     * @param methodName  服务中的方法名称,必须非空
     * @param id          指定的id
     * @param parameters  服务的参数,如果为空则表示无参数(void)
     */
    public Request(String serviceName, String methodName, String methodId, int id, Object[] parameters) {
        Preconditions.checkArgument(serviceName != null && !serviceName.isEmpty(), "The serviceName must be set.");
        Preconditions.checkArgument(methodName != null && !methodName.isEmpty(), "The serviceName must be set.");
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.methodId = methodId;
        this.id = id;
        this.parameters = parameters;
    }


    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String getMethodId() {
        return methodId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        if (!super.equals(o)) return false;

        Request request = (Request) o;

        if (methodName != null ? !methodName.equals(request.methodName) : request.methodName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parameters, request.parameters)) return false;
        if (serviceName != null ? !serviceName.equals(request.serviceName) : request.serviceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
        return result;
    }

}
