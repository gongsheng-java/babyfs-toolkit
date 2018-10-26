package com.babyfs.tk.apollo;

import com.alipay.api.internal.util.StringUtils;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.lang.text.StrSubstitutor;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import static com.babyfs.tk.apollo.EnvConstants.*;

public class ConfigLoader {

    private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static boolean isApolloReady = false;

    private static boolean isDevEnv = false;

    private static Map<String, ConfigCache> configCacheMap = Maps.newConcurrentMap();

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";

    public static String DEPART_NAME;

    private static Map<String, Map<String, String>> devLocalConfig;

    static{
//        loadDevProperties();
//        try{
//            ConfigCache defaultConfig = new ConfigCache();
//            injectDev(DEFAULT_NAMESPACE, defaultConfig);
//            configCacheMap.put(DEFAULT_NAMESPACE, defaultConfig);
//            Assert.notNull(defaultConfig);
//            isApolloReady = true;
//            initSystemProperty(defaultConfig);
//
//        }catch (Exception e){
//            logger.warn("get config error", e);
//        }
    }

    public static boolean isApolloReady(){
        return isApolloReady;
    }

    private static void initSystemProperty(ConfigCache config){

        String department = config.getConfig(KEY_SYSTEM_DEPART_NAME);
        if(Strings.isNullOrEmpty(department)){
            logger.warn("cannot find depart name config, use default depart name as babyfs");
            DEPART_NAME = DEFAULT_DEPARTNAME;
        }else{
            DEPART_NAME = department;
        }

    }

    /**
     * 加载本地Dev环境配置
     */
    private static void loadDevProperties(){
        try{
            Properties devProperteis = new Properties();
            try(InputStream is = Resources.asByteSource(Resources.getResource(OVERRIDE_PROPS_FILE)).openStream()){
                devProperteis.load(is);
                if(devProperteis.size() > 0){
                    isDevEnv = true;
                    devLocalConfig = Maps.newHashMap();
                    Set<Map.Entry<Object, Object>> entries = devProperteis.entrySet();
                    for (Map.Entry<Object, Object> entry:
                         entries) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        buildDevMap(devLocalConfig, key , value);
                    }
                }
            }

        }catch (Exception e){
            logger.warn("load dev config failed", e);
            isDevEnv = false;
        }

    }

    /**
     * 判断是否要使用dev配置
     * @param namespace
     * @param configCache
     */
    private static void injectDev(String namespace, ConfigCache configCache){
        if(!isDevEnv) return;
        Map<String, String> map = devLocalConfig.get(namespace);
        if(map == null){
            return;
        }
        configCache.setDev(map);
    }

    private static void buildDevMap(Map<String, Map<String, String>> devLocalConfig, String key, String value){
        if(StringUtils.areNotEmpty(key, value)){
            String[] split = key.split(OVERRIDE_SPLIT);
            if(split.length != 2){
                return;
            }
            String namespace = split[0], subKey = split[1];
            Map<String, String> container = devLocalConfig.get(namespace);
            if(container == null){
                container = Maps.newHashMap();
                devLocalConfig.put(namespace, container);
            }

            container.put(subKey, value);
        }

    }

    public static Map<String, String> getMap(){
        return getMap(DEFAULT_NAMESPACE);
    }

    public static Map<String,String> getMap(String nameSpace){
        if(!isApolloReady) {
            return Maps.newHashMap();
        }
        return getOrBuildConfigCache(nameSpace).getConfigMap();
    }

    private static ConfigCache getOrBuildConfigCache(String namespace){

        ConfigCache configCache = configCacheMap.get(namespace);

        if(configCache != null){
            return configCache;
        }
        synchronized (configCacheMap){
            configCache = new ConfigCache(namespace);
            injectDev(namespace, configCache);
            configCacheMap.put(namespace, configCache);
        }

        return configCache;
    }

    /**
     * replace the place holder
     * @param content
     * @param enableSubstitutionInVariables tell if replace a placeholder in another
     * @return
     */
    public static String replacePlaceHolder(String content, boolean enableSubstitutionInVariables){
        if(!isApolloReady) return content;

        Map<String, String> plainConfigMap = getMap();
        return replacePlaceHolder0(plainConfigMap, content, enableSubstitutionInVariables);
    }

    public static String replacePlaceHolder(Map<String, String> map, String content, boolean enableSubstitutionInVariables){
        return replacePlaceHolder0(map, content, enableSubstitutionInVariables);
    }

    public static String replacePlaceHolder(Map<String, String> map, String content){
        return replacePlaceHolder(map, content, false);
    }

    private static String replacePlaceHolder0(Map<String, String> plainConfigMap, String content, boolean enableSubstitutionInVariables){
        if(CollectionUtils.isEmpty(plainConfigMap)) return content;
        StrSubstitutor substitutor = new StrSubstitutor(plainConfigMap, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        substitutor.setEnableSubstitutionInVariables(enableSubstitutionInVariables);
        return substitutor.replace(content);
    }

    /**
     * replace the place holder
     * @param content
     * @return
     */
    public static String replacePlaceHolder(String content){
        if(!isApolloReady) return content;
        return replacePlaceHolder(content, false);
    }

    public static String replacePlaceHolder(String namespace, String content){
        return replacePlaceHolder(namespace, content, false);
    }

    public static String replacePlaceHolder(String namespace, String content, boolean enableSubstitutionInVariables){
        if(!isApolloReady) return content;
        Map<String, String> plainConfigMap = getMap(namespace);
        return replacePlaceHolder0(plainConfigMap, content, enableSubstitutionInVariables);
    }

    public static <T> void watch(String namespace, Class<T> tClass, Consumer<T> consumer){
        Assert.isTrue(isApolloReady, "apollo is not ready");
        ConfigCache configCache = getOrBuildConfigCache(namespace);
        configCache.watch(tClass, consumer);
    }

    public static <T> void watch(Class<T> tClass, Consumer<T> consumer) {
        watch(DEFAULT_NAMESPACE, tClass, consumer);
    }
    /**
     * get a value by certain type
     * @param namespace
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T>  T getConfig(String namespace, Class<T> tClass) {
        Assert.isTrue(isApolloReady, "apollo is not ready");
        ConfigCache configCache = getOrBuildConfigCache(namespace);
        return configCache.getConfig(tClass);
    }

    public static <T>  T  getConfig(Class<T> tClass) {
        return getConfig(DEFAULT_NAMESPACE, tClass);
    }

    /**
     * get the value from a config service.
     * if the namespace is set , the value will override the default namespace
     * this procedure is not reversible, which is to say. if you delete the value in the namespace, the framework would not
     * get the value by the same key from the default namespace
     * @param key
     * @param namespace
     * @return
     */
    public static String getConfig(String namespace, String key){
        Assert.isTrue(isApolloReady, "apollo is not ready");
        ConfigCache configCache = getOrBuildConfigCache(namespace);
        return configCache.getConfig(key);
    }

    public static String getConfig(String key){
        return getConfig(DEFAULT_NAMESPACE, key);
    }

}
