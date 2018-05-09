package com.babyfs.tk.galaxy.server.impl;

import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.galaxy.RpcCodec;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.Utils;
import com.babyfs.tk.galaxy.constant.RpcConstant;
import com.babyfs.tk.galaxy.register.IServcieRegister;
import com.babyfs.tk.galaxy.server.IServer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * rpc server接口实现
 */
@Order
public class ServerImpl extends LifeServiceSupport implements IServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerImpl.class.getName());

    private ICodec codec;


    private final Map<ServicePoint, Object> services;

    private volatile Map<String, Dispatcher> serviceDispatchers;

    @Inject(optional = true)
    private IServcieRegister servcieRegister;

    @Inject
    public ServerImpl(@Named(RpcConstant.NAME_RPC_SERVER_EXPOSE) Map<ServicePoint, Object> services, @RpcCodec ICodec codec) {
        this.services = services;
        this.codec = Preconditions.checkNotNull(codec);
    }


    @Override
    @SuppressWarnings("unchecked")
    public ServiceResponse<byte[]> handle(byte[] content) {
        if (content == null || content.length == 0) {
            return ServiceResponse.PARAM_ERROR_RESPONSE;
        }

        Object decoded = codec.decode(content);
        if (!(decoded instanceof RpcRequest)) {
            return ServiceResponse.PARAM_ERROR_RESPONSE;
        }

        ServiceResponse<Object> ret = this.handle((RpcRequest) decoded);
        if (ret.isFailure()) {
            return ServiceResponse.createFailResponse(ret.getCode(), ret.getMsg());
        } else {
            byte[] code = codec.encode(ret.getData());
            return ServiceResponse.createSuccessResponse(code);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServiceResponse<Object> handle(RpcRequest request) {
        if (request == null || Strings.isNullOrEmpty(request.getInterfaceName()) || Strings.isNullOrEmpty(request.getMethodSign())) {
            return ServiceResponse.PARAM_ERROR_RESPONSE;
        }

        final String interfaceName = request.getInterfaceName();
        final String methodSign = request.getMethodSign();

        final Map<String, Dispatcher> dispatchers = this.serviceDispatchers;
        if (dispatchers == null) {
            return ServiceResponse.createFailResponse("no service");
        }

        Dispatcher dispatcher = dispatchers.get(interfaceName);
        if (dispatcher == null) {
            return ServiceResponse.createFailResponse("no service found for " + interfaceName);
        }

        Method method = dispatcher.methods.get(methodSign);
        if (method == null) {
            return ServiceResponse.createFailResponse("no service method found for " + interfaceName + " " + methodSign);
        }

        try {
            Object ret;
            if (request.getParameters() == null) {
                ret = method.invoke(dispatcher.svr);
            } else {
                ret = method.invoke(dispatcher.svr, request.getParameters());
            }
            return ServiceResponse.createSuccessResponse(ret);
        } catch (Exception e) {
            LOGGER.error("handler request {} fail", request, e);
            return ServiceResponse.createFailResponse(500, "excepition:" + e + ",cause:" + e.getCause());
        }
    }

    public synchronized IServcieRegister getServcieRegister() {
        return servcieRegister;
    }

    public synchronized void setServcieRegister(IServcieRegister servcieRegister) {
        this.servcieRegister = servcieRegister;
    }

    @Override
    protected void execStart() {
        super.execStart();
        Map<String, Dispatcher> serviceDispatcher = Maps.newHashMap();
        for (Map.Entry<ServicePoint, Object> entry : this.services.entrySet()) {
            ServicePoint p = Preconditions.checkNotNull(entry.getKey());
            Object svr = Preconditions.checkNotNull(entry.getValue());
            Class type = p.getType();

            String interfaceName = p.getInterfaceName();
            Preconditions.checkState(type.isAssignableFrom(svr.getClass()), "interface name %s", interfaceName);

            Map<String, Method> methods = parseMethods(type);
            serviceDispatcher.put(interfaceName, new Dispatcher(methods, svr));

            LOGGER.info("add rpc service {} ", interfaceName);
        }

        this.serviceDispatchers = serviceDispatcher;

        if (this.servcieRegister != null) {
            ArrayList<String> serviceNames = Lists.newArrayList(this.serviceDispatchers.keySet());
            LOGGER.info("add {} to servcie register", serviceNames);
            this.servcieRegister.addServices(serviceNames);
        } else {
            LOGGER.info("not service register,ignore it");
        }
    }

    @Override
    protected void execStop() {
        super.execStop();
        this.serviceDispatchers = null;
    }

    /**
     * 解析接口的方法
     *
     * @param interfaceType
     * @return
     */
    private Map<String, Method> parseMethods(final Class interfaceType) {
        Preconditions.checkArgument(interfaceType.isInterface(), "only interface can be proxy,%s", interfaceType);
        Map<String, Method> methods = Maps.newHashMap();

        Utils.parseMethods(interfaceType, meta -> {
            methods.put(meta.getSig(), meta.getMethod());
            return null;
        });
        return methods;
    }

    private class Dispatcher {
        private final Map<String, Method> methods;
        private final Object svr;

        private Dispatcher(Map<String, Method> methods, Object svr) {
            this.methods = Preconditions.checkNotNull(methods);
            this.svr = Preconditions.checkNotNull(svr);
        }
    }
}
