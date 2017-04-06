package com.babyfs.tk.rpc.service;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.JavaProxyUtil;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.concurrent.CallbackUtils;
import com.babyfs.tk.commons.concurrent.ICallback;
import com.babyfs.tk.commons.name.INameService;
import com.babyfs.tk.commons.name.Server;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.babyfs.tk.rpc.NameContext;
import com.babyfs.tk.rpc.Request;
import com.babyfs.tk.rpc.Response;
import com.babyfs.tk.rpc.client.RPCClient;
import com.babyfs.tk.rpc.codec.Codecs;
import com.babyfs.tk.rpc.service.internal.AsyncResponseReceiver;
import com.babyfs.tk.rpc.service.internal.ResponseReceiver;
import com.babyfs.tk.rpc.service.internal.SyncResponseReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;

/**
 * 客户端的服务代理,所有服务的调用者由该类代理想服务器请求
 */
public class ClientServiceProxy extends BaseServiceProxy implements IRPCListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServiceProxy.class);

    /**
     * 序列化的编码方式
     */
    private final byte codecType;
    /**
     * 命名服务
     */
    private final INameService nameService;
    /**
     * RPC客户端
     */
    private final RPCClient rpcClient;
    /**
     * 记录发出的请求
     */
    private final ConcurrentMap<Integer, ResponseReceiver> requests = Maps.newConcurrentMap();
    /**
     * 请求的超时时间,ms
     */
    private final long timeout;
    /**
     * 一般的server查找策略
     */
    private final CommonServerLookup commonServerLookup = new CommonServerLookup();
    /**
     * 保持粘性的server查找策略
     */
    private final StickyServerLookup stickyServerLookup = new StickyServerLookup();
    /**
     * 严格的根据serverId查找server的策略
     */
    private final RestrictServerLookup restrictServerLookup = new RestrictServerLookup();
    /**
     * 进行维护工作的线程池
     */
    private final ScheduledExecutorService maintainExecutorService;
    /**
     * 异步调用情景下的结果分发线程池,目的是为了防止回调接口的业务逻辑操作阻塞I/O线程
     */
    private final ExecutorService asyncDispatchExecutor;

    /**
     * @param codecType   数据编码的类型
     * @param nameService 命名服务
     * @param rpcClient   RPC客户端
     * @param timeout     RPC调用超时,单位ms
     */
    public ClientServiceProxy(byte codecType, INameService nameService, RPCClient rpcClient, long timeout) {
        this(codecType, nameService, rpcClient, timeout,
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("CSProxy-SingleMaintainThread")),
                Executors.newSingleThreadExecutor(new NamedThreadFactory("CSProxy-SingleAsyncDispatch")));
    }

    /**
     * @param codecType               数据编码的类型
     * @param nameService             命名服务
     * @param rpcClient               RPC客户端
     * @param timeout                 RPC调用超时,单位ms
     * @param maintainExecutorService rpc维护线程池
     * @param asyncDispatcherExecutor 异步调用结果分发线程池
     */
    public ClientServiceProxy(byte codecType, INameService nameService, RPCClient rpcClient, long timeout, ScheduledExecutorService maintainExecutorService, ExecutorService asyncDispatcherExecutor) {
        Preconditions.checkArgument(Codecs.getCodecByType(codecType) != null, "Not a valid codec type [" + codecType + "]");
        Preconditions.checkArgument(nameService != null, "The nameService must not be null");
        Preconditions.checkArgument(rpcClient != null, "The rpc client must not be null.");
        Preconditions.checkArgument(timeout > 0, "The timeout must >0.");
        Preconditions.checkArgument(maintainExecutorService != null, "maintainExecutorService");
        this.timeout = timeout;
        this.codecType = codecType;
        this.nameService = nameService;
        this.rpcClient = rpcClient;
        this.maintainExecutorService = maintainExecutorService;
        this.asyncDispatchExecutor = asyncDispatcherExecutor;
    }

    @Override
    public Object callService(String serviceName, String methodName, String methodId, Object[] args) {
        return call(serviceName, methodName, methodId, args, commonServerLookup);
    }

    /**
     * 调用带有一定粘性的服务,即可以指定每次调用时的目标服务器,如果指定的服务器不存在,则尝试寻找其他提供服务接口的server
     *
     * @param serviceName 服务名称
     * @param methodName  服务的方法名
     * @param methodId    方法id,用于区分重载的方法
     * @param args        方法调用的参数
     * @return
     */
    public Object callStickyService(String serviceName, String methodName, String methodId, Object[] args) {
        return call(serviceName, methodName, methodId, args, stickyServerLookup);
    }

    /**
     * 使用serverId进行严格查找的server,如果指定的serverId不存在,则直接调用失败
     *
     * @param serviceName
     * @param methodName
     * @param methodId
     * @param args
     * @return
     */
    public Object callRestrictService(String serviceName, String methodName, String methodId, Object[] args) {
        return call(serviceName, methodName, methodId, args, restrictServerLookup);
    }

    public void receive(Response response) {
        ResponseReceiver pre = requests.remove(response.getId());
        if (pre == null) {
            return;
        }
        if (!response.isParamterParsed() && response.getData() != null) {
            ICodec codec = Codecs.getCodecByType(response.getCodecType());
            try {
                int length = response.getData().readInt();
                if (length > 0) {
                    byte[] data = new byte[length];
                    response.getData().readBytes(data);
                    ServiceWrapper serviceWrapper = get(pre.getServiceName());
                    ServiceWrapper.MethodWrapper serviceMethod = serviceWrapper.getServiceMethod(pre.getMethodName(), pre.getMethodId());
                    Object decode = codec.decode(data, serviceMethod.getReturnInstanceCreator());
                    response.setResponse(decode);
                }
            } catch (Exception e) {
                LOGGER.error("Decode unparsed response error.", e);
                response.setSuccess(false);
                response.setErrormsg(e.getMessage());
            } finally {
                response.setParamterParsed(true);
                response.setData(null);
            }
        }
        pre.onFinish(null, response);
    }

    /**
     * 构建一般的servcie代理接口
     *
     * @param serviceName           服务名称
     * @param serviceInterfaceClass
     * @param <T>
     * @return
     */
    public <T> T buildProxy(final String serviceName, final Class<T> serviceInterfaceClass) {
        return buildProxy(serviceName, serviceInterfaceClass, ClientProxyStrategy.AUTO);
    }

    /**
     * 构建指定类型的service代理接口
     *
     * @param serviceName
     * @param serviceInterfaceClass
     * @param proxyStrategy
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T buildProxy(final String serviceName, final Class<T> serviceInterfaceClass, ClientProxyStrategy proxyStrategy) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceName), "serviceName");
        Preconditions.checkArgument(serviceInterfaceClass != null, "serviceInterfaceClass");
        Preconditions.checkArgument(proxyStrategy != null, "proxyStrategy");
        super.add(serviceName, serviceInterfaceClass);
        Class<?>[] interfaces = {serviceInterfaceClass};
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, new ProxyHandler(this, serviceName, interfaces, proxyStrategy));
    }

    public void onResponseReceived(Response response) {
        receive(response);
    }

    public void onRequestReceived(Request request) {
        throw new UnsupportedOperationException();

    }

    private Object call(String serviceName, String methodName, String methodId, Object[] args, Function<String, Server> lookUpFunction) {
        /**
         * 根据是否有回调接口决定使用同步还是异步调用
         */
        if (CallbackUtils.getUpCallbackOnCurThread() == null) {
            return callInSync(serviceName, methodName, methodId, args, lookUpFunction);
        } else {
            callInAsync(serviceName, methodName, methodId, args, lookUpFunction);
            return null;
        }
    }

    /**
     * @param serviceName
     * @param methodName
     * @param methodId
     * @param args
     * @param lookUpFunction
     * @return
     * @throws ServiceException
     */
    private Object callInSync(String serviceName, String methodName, String methodId, Object[] args, Function<String, Server> lookUpFunction) {
        // 性能监控参数
        final Server server = lookUpFunction.apply(serviceName);
        if (server == null) {
            throw new ServiceException("Can't find the Server for service [" + serviceName + "] with lookup function [" + lookUpFunction.getClass() + "]");
        }
        final String address = server.getIp();
        final int port = server.getPort();

        Request request = new Request(serviceName, methodName, methodId, args);
        request.setCodecType(this.codecType);

        SyncResponseReceiver receiver = new SyncResponseReceiver(serviceName, methodName, methodId);
        requests.put(request.getId(), receiver);
        try {
            try {
                rpcClient.sendRequest(address, port, request);
                boolean waitRet = receiver.getCountDownLatch().await(timeout, TimeUnit.MILLISECONDS);
                LOGGER.debug("Received before time out:{}", waitRet);
            } catch (Exception e) {
                throw new ServiceException("Send request id " + request.getId() + " error.", e);
            } finally {
                requests.remove(request.getId());
            }
            Response response = receiver.getResponse();
            if (response != null) {
                if (response.isSuccess()) {
                    return response.getResponse();
                } else {
                    throw new ServiceException(response.getErrormsg());
                }
            }
            //还没有响应
            throw new ServiceException("Time out for " + serviceName + "." + methodName + " request id " + request.getId());
        } finally {
        }
    }

    private void callInAsync(String serviceName, String methodName, String methodId, Object[] args, Function<String, Server> lookUpFunction) {
        // 性能监控参数
        final long st = System.nanoTime();
        final String probeItemName = serviceName + "_" + methodName;
        final Server server = lookUpFunction.apply(serviceName);
        if (server == null) {
            throw new ServiceException("Can't find the Server for service [" + serviceName + "] with lookup function [" + lookUpFunction.getClass() + "]");
        }
        final ICallback realCallback = CallbackUtils.getUpCallbackOnCurThread();
        Preconditions.checkState(realCallback != null, "realCallback is null,please see use CallbackUtils.setUpCallackOnCurThread() first.");
        final String address = server.getIp();
        final int port = server.getPort();
        final Request request = new Request(serviceName, methodName, methodId, args);
        final AsyncCallback asyncCallback = new AsyncCallback(probeItemName, st, realCallback);
        FutureTask<Void> timeOutTask = null;
        if (timeout > 0) {
            timeOutTask = new FutureTask<Void>(new TimedOutRunnable(request, asyncCallback), null);
            asyncCallback.setTimeOutTask(timeOutTask);
        }
        request.setCodecType(this.codecType);
        AsyncResponseReceiver receiver = new AsyncResponseReceiver(serviceName, methodName, methodId, asyncCallback);
        requests.put(request.getId(), receiver);
        try {
            rpcClient.sendRequest(address, port, request);
        } catch (Exception e) {
            LOGGER.error("Send request " + serviceName + "." + methodName + " fail.", e);
            requests.remove(request.getId());
            asyncCallback.onException(null, new ServiceException("Send request id " + request.getId() + " error.", e));
            return;
        }
        if (timeOutTask != null) {
            //开始超时调度
            maintainExecutorService.schedule(timeOutTask, timeout, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 无状态的ProxyHandler,目标服务器由{@link INameService#findServerByServiceName(String)}确定
     */
    private static class ProxyHandler implements InvocationHandler {
        protected final ClientServiceProxy clientServiceProxy;
        protected final String serviceName;
        protected final Class[] interfaces;
        protected final ServiceWrapper serviceWrapper;
        protected final ClientProxyStrategy proxyStrategy;

        public ProxyHandler(ClientServiceProxy clientServiceProxy, String serviceName, Class[] interfaces, ClientProxyStrategy proxyStrategy) {
            this.clientServiceProxy = clientServiceProxy;
            this.serviceName = serviceName;
            this.interfaces = interfaces;
            this.proxyStrategy = proxyStrategy;
            serviceWrapper = clientServiceProxy.services.get(serviceName);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodId = serviceWrapper.getServiceMethodId(method);
            if (methodId != null) {
                switch (this.proxyStrategy) {
                    case AUTO:
                        return clientServiceProxy.callService(serviceName, method.getName(), methodId, args);
                    case STICKY:
                        return clientServiceProxy.callStickyService(serviceName, method.getName(), methodId, args);
                    case RESTRICT:
                        return clientServiceProxy.callRestrictService(serviceName, method.getName(), methodId, args);
                    default:
                        throw new RuntimeException("Unknown proxy type:" + proxyStrategy);
                }
            } else {
                return JavaProxyUtil.invokeMethodOfObject(proxy, method, args, interfaces);
            }
        }
    }

    private class AsyncCallback implements ICallback {
        private final String probeItemName;
        private final long st;
        private final ICallback callback;
        private FutureTask<Void> timeOutTask;

        public AsyncCallback(String probeItemName, long st, ICallback callback) {
            this.probeItemName = probeItemName;
            this.st = st;
            this.callback = callback;
        }

        @Override
        public void onFinish(final Object o, final Object out) {
            if (timeOutTask != null) {
                this.timeOutTask.cancel(true);
            }
            asyncDispatchExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onFinish(o, out);
                }
            });
        }

        @Override
        public void onException(final Object o, final Exception e) {
            if (timeOutTask != null) {
                this.timeOutTask.cancel(true);
            }
            asyncDispatchExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onException(o, e);
                }
            });
        }

        public void setTimeOutTask(FutureTask<Void> timeOutTask) {
            this.timeOutTask = timeOutTask;
        }
    }


    private class CommonServerLookup implements Function<String, Server> {
        @Override
        public Server apply(@Nullable String serviceName) {
            return nameService.findServerByServiceName(serviceName);
        }
    }

    private class StickyServerLookup implements Function<String, Server> {
        @Override
        public Server apply(@Nullable String serviceName) {
            String serverId = NameContext.getCurServerId();
            if (serverId != null) {
                Server server = nameService.findServerByServerId(serviceName, serverId);
                if (server != null) {
                    return server;
                }
            }
            Server server = nameService.findServerByServiceName(serviceName);
            if (server != null) {
                NameContext.setNewServerId(server.getId());
            }
            return server;
        }
    }

    private class RestrictServerLookup implements Function<String, Server> {
        @Override
        public Server apply(@Nullable String serviceName) {
            String serverId = NameContext.getCurServerId();
            if (serverId != null) {
                Server server = nameService.findServerByServerId(serviceName, serverId);
                if (server != null) {
                    return server;
                } else {
                    LOGGER.warn("Can't find the server for serverId:{},serviceName:{}.", serverId, serviceName);
                }
            } else {
                LOGGER.warn("Can't find the serverId at NameContext for serviceName:{},maybe forget call NameContext.setServerId before calling the service method.", serviceName);
            }
            return null;
        }
    }

    private class TimedOutRunnable implements Runnable {
        private final Request request;
        private final ICallback callback;

        public TimedOutRunnable(Request request, ICallback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void run() {
            ResponseReceiver remove = requests.remove(request.getId());
            if (remove != null) {
                callback.onException(null, new ServiceException("Time out for " + request.getServiceName() + "." + request.getMethodName() + " request id " + request.getId()));
            }
        }
    }
}
