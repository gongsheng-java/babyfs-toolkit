package com.babyfs.tk.galaxy;

import com.babyfs.tk.galaxy.client.impl.MethodMeta;
import com.babyfs.tk.rpc.util.ReflectionUtil;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

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
}
