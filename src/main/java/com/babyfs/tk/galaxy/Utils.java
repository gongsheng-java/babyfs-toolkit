package com.babyfs.tk.galaxy;

import com.babyfs.tk.galaxy.client.impl.MethodMeta;
import com.babyfs.tk.rpc.util.ReflectionUtil;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * 工具类
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    /**
     * 禁止代理的方法集合:
     * {@link Object}中的方法都不代理
     */
    public static final ImmutableSet<Method> FORBIDDEN_METHODS = new ImmutableSet.Builder<Method>().add(Object.class.getMethods()).build();

    /**
     * 生成代理对象的方法签名
     *
     * @param method
     * @return
     */
    public static String methodSig(Method method) {
        return method.getName() + "#" + ReflectionUtil.methodSignature(method);
    }

    /**
     * 构建服务接口的名称
     *
     * @param interfaceType
     * @param name
     * @return
     */
    public static String buildServcieName(Class interfaceType, String name) {
        if (Strings.isNullOrEmpty(name)) {
            return interfaceType.getName();
        }
        return interfaceType.getName() + "/" + name;
    }

    /**
     * 解析接口的方法
     *
     * @param interfaceType
     * @param processor
     */
    public static void parseMethods(final Class interfaceType, Function<MethodMeta, Void> processor) {
        Preconditions.checkArgument(interfaceType.isInterface(), "only interface can be proxy,%s", interfaceType);

        Set<String> methodSet = Sets.newHashSet();
        for (Method method : interfaceType.getMethods()) {
            //不处理FORBIDDEN_METHODS
            if (FORBIDDEN_METHODS.contains(method)) {
                LOGGER.info("skip {}", method);
                continue;
            }

            String methodSig = Utils.methodSig(method);
            checkState(methodSet.add(methodSig), "duplicate method:%s", method);
            MethodMeta metadata = new MethodMeta(method, methodSig);
            processor.apply(metadata);
        }
    }

    /**
     * 构建curator
     *
     * @param zkRegisterUrl
     * @param connectTimeout
     * @param sessionTimeout
     * @return
     */
    public static CuratorFramework buildAndStartCurator(String zkRegisterUrl, int connectTimeout, int sessionTimeout) {
        checkNotNull(zkRegisterUrl, "zkRegisterUrl");
        checkState(connectTimeout > 0, "connectTimeout > 0");
        checkState(sessionTimeout > 0, "sessionTimeout > 0");

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(zkRegisterUrl).retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectTimeout)
                .sessionTimeoutMs(sessionTimeout)
                .build();
        curator.start();
        try {
            boolean connected = curator.blockUntilConnected(connectTimeout, TimeUnit.MILLISECONDS);
            LOGGER.info("connect to zk {},success {}", zkRegisterUrl, connected);
            if (!connected) {
                throw new RuntimeException("can't connect to zk in " + connectTimeout + " mills");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("can't connect to zk", e);
        }
        return curator;
    }
}
