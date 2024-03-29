package com.babyfs.tk.galaxy.client.impl;


import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.galaxy.RpcException;
import com.babyfs.tk.galaxy.RpcRequest;
import com.babyfs.tk.galaxy.ServicePoint;
import com.babyfs.tk.galaxy.client.IClient;
import com.babyfs.tk.galaxy.register.ILoadBalance;
import com.babyfs.tk.galaxy.register.ServiceServer;
import com.babyfs.tk.probe.metrics.MetricsProbe;
import com.google.common.base.Splitter;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 被代理对象方法的实际执行handler
 */
public final class MethodHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandler.class);
    private final ServicePoint<?> target;
    private final ICodec codec;
    private final MethodMeta metadata;
    private final IClient client;
    private final ILoadBalance loadBalance;
    private final String urlPrefix;

    static final Summary rpcCallLatency = Summary.build()
            .name("rpc_call_latency_seconds")
            .labelNames("method", "target_server", "success")
            .quantile(0.98, 0.05)
            .quantile(0.85, 0.05)
            .quantile(0.50, 0.05)
            .help("Request latency in seconds.").register();

    public MethodHandler(ServicePoint<?> target, ICodec codec, IClient client, MethodMeta metadata, ILoadBalance loadBalance, String urlPrefix) {
        this.target = checkNotNull(target, "target");
        this.codec = checkNotNull(codec, "codec");
        this.metadata = checkNotNull(metadata, "metadata");
        this.client = checkNotNull(client, "client");
        this.loadBalance = checkNotNull(loadBalance, "loadBalance");
        this.urlPrefix = checkNotNull(urlPrefix, "urlPrefix");

    }

    /**
     * rpc 代理对象的方法实际执行方法
     * 执行编码，远程调用，解码
     *
     * @param argv
     * @return
     * @throws Throwable
     */
    public Object invoke(Object[] argv) {
        byte[] body = codec.encode(createRequest(argv));
        String interfaceName = target.getType().getName();
        String url = "";
        int retryCount = 0;
        Set<ServiceServer> exceptionServerSet = null;
        ServiceServer serviceServer = null;
        while (true) {
            try {
                if(exceptionServerSet != null && exceptionServerSet.size() > 0){
                    serviceServer = loadBalance.findServerAfterFilter(interfaceName, exceptionServerSet);
                }else {
                    serviceServer = loadBalance.findServer(interfaceName);
                }
                if(serviceServer == null){
                    LOGGER.error("rpc connect remote url :{},tryCount:{}, no service server is available", url,retryCount);
                    throw new RpcException(String.format("rpc invoke remote method fail after %s try, no service server is available",retryCount));
                }
                url = getExecuteUrl(interfaceName, serviceServer);
                byte[] content = client.execute(url, body);
                return codec.decode(content);
            }
            catch (java.net.ConnectException e) {
                if(retryCount==2){
                    LOGGER.error("rpc connect remote url :{},tryCount:{}", url,retryCount);
                    LOGGER.error("rpc invoke remote method fail,tryCount:{}",retryCount, e);
                    throw new RpcException(String.format("rpc invoke remote method fail after %s try",retryCount),e);
                }
                LOGGER.warn("rpc connect error, retry to connect again, mark service server [{}] with unavailable", serviceServer.toString());
                if(exceptionServerSet == null){
                    exceptionServerSet = new HashSet<>();
                }
                exceptionServerSet.add(serviceServer);
            }
            catch (Exception e) {
                LOGGER.error("rpc connect remote url :{}", url);
                LOGGER.error("rpc invoke remote method fail", e);
                throw new RpcException("rpc invoke remote method fail", e);
            } finally {
                retryCount++;
            }
        }
    }

    private String getExecuteUrl(String interfaceName, ServiceServer serviceServer) {
        if (serviceServer == null) {
            LOGGER.error("no serviceInstance for {}", interfaceName);
            throw new RpcException("no serviceInstance for:" + interfaceName);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceServer.getHost()).append(":").append(serviceServer.getPort());
        return stringBuilder.append(urlPrefix).toString();
    }

    /**
     * 根据方法传入的实际参数，与方法元数据pojo构造RpcRequest
     *
     * @param argv
     * @return
     */
    private RpcRequest createRequest(Object[] argv) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(argv);
        rpcRequest.setMethodSign(metadata.getSig());
        rpcRequest.setInterfaceName(target.getInterfaceName());
        return rpcRequest;
    }

    private void oldMetric(RpcRequest request, ServiceServer server, long start, boolean success) {
        MetricsProbe.timerUpdateNSFromStart("rpc", getMetricItemName(request, server), start, success);
    }

    /**
     * @param request
     * @param server
     * @param start
     * @param success
     */
    private void metric(RpcRequest request, ServiceServer server, long start, boolean success) {
        String className = request.getInterfaceName();
        int dotIndex = className.lastIndexOf('.');
        String simpleName = (dotIndex == -1) ? className : className.substring(dotIndex + 1);
        String simpleMethod = Splitter.on("#").splitToList(request.getMethodSign()).get(0);

        String method = simpleName + "." + simpleMethod;
        String targetServer = server.getHost() + ":" + server.getPort();

        rpcCallLatency.labels(method, targetServer, success ? "1" : "0").observe((System.nanoTime() - start) / 1.0E9D);
    }

    /**
     * 构建itemName
     *
     * @param request
     * @param server
     * @return
     */
    private String getMetricItemName(RpcRequest request, ServiceServer server) {
        String className = request.getInterfaceName();
        int dotIndex = className.lastIndexOf('.');
        String simpleName = (dotIndex == -1) ? className : className.substring(dotIndex + 1);
        String simpleMethod = Splitter.on("#").splitToList(request.getMethodSign()).get(0);

        StringBuilder builder = new StringBuilder();
        builder.append("RPC").append(".")
                .append(server.getHost()).append(".")
                .append(server.getPort()).append(".")
                .append(simpleName).append(".")
                .append(simpleMethod);
        return builder.toString();
    }
}
