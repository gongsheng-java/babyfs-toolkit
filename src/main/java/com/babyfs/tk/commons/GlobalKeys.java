package com.babyfs.tk.commons;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.babyfs.tk.commons.application.AppLauncerModule;
import com.babyfs.tk.commons.guice.GuiceKeys;

import java.util.Map;

/**
 * 定义全局的Guice 绑定名称
 * <p/>
 */
public final class GlobalKeys {
    /**
     * 全局启动参数的绑定名称,通常是解析main(String[] args)得到
     */
    public static final String APP_GLOBAL_ARG_KEY_NAME = "APP.GLOBAL.ARG";
    /**
     * 数据验证规则配置文件位置的绑定名称，值的格式是逗号分割的String，如：
     * file
     * file1,file2
     */
    public static final String VALIDATION_RULE_CONF = "VALIDATION.RULE.CONF";
    /**
     * 数据验证规则严格模式
     */
    public static final String VALIDATION_STRICT_MODE = "VALIDATION.STRICT.MODE";
    /**
     * 包含请求参数元数据的Bean类数组
     */
    public static final String PARAM_BEAN_CLASSES = "PARAM.BEAN.CLASSES";
    /**
     * 请求参数Bean接口和对应实现类的Map
     */
    public static final String PARAM_BEAN_IMPL_MAP = "PARAM.BEAN.IMPL.MAP";
    /**
     * Guice binding:Application的启动参数ImmutableMap的Key
     *
     * @see {@link #APP_GLOBAL_ARG_KEY_NAME}
     * @see {@link AppLauncerModule#configure()}
     */
    public static final Key<ImmutableMap<String, String>> APP_GLOBAL_ARG_KEY = GuiceKeys.getKey(ImmutableMap.class, Names.named(APP_GLOBAL_ARG_KEY_NAME), String.class, String.class);

    public static final Key<Map<Class, Class>> PARAM_BEAN_IMPL_MAP_KEY = GuiceKeys.getKey(Map.class, Names.named(PARAM_BEAN_IMPL_MAP), Class.class, Class.class);

    private GlobalKeys() {

    }
}
