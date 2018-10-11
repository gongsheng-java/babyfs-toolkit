package com.babyfs.tk.apollo;


import com.babyfs.tk.apollo.annotation.ConfigKey;
import com.babyfs.tk.apollo.parser.ParserFactory;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConfigLoader {

    private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static boolean hasNameSpace = false;
    private static String nameSpace = null;
    private static Config nameSpaceConfigService;

    private static Config configService;

    private static volatile AtomicBoolean hasStartWatchThread = new AtomicBoolean(false);

    private static Map<String, WatchCache[]> watchCacheMap = Maps.newHashMap();

    private static Map<String, String> plainConfigMap;

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";

    static{
        String inComeNameSpace = System.getProperty(EnvConstants.SPK_NAMESPACE);
        if(inComeNameSpace != null){
            hasNameSpace = true;
            nameSpace = inComeNameSpace;
            try{
                nameSpaceConfigService = ConfigService.getConfig(nameSpace);
            }catch (Exception e){
                nameSpaceConfigService = null;
                hasNameSpace = false;
                logger.warn("get namespace config error", e);
            }
        }
        try{
            configService = ConfigService.getAppConfig();
        }catch (Exception e){
            logger.warn("get config error", e);
            configService = null;
        }

        if(configService != null){
            buildMap();
            startWatchThread();
        }
    }

    private static void buildMap(){
        Map<String, String> builder = Maps.newConcurrentMap();
        if(hasNameSpace){
            buildConfig(builder, nameSpaceConfigService);
        }
        buildConfig(builder, configService);

        plainConfigMap = builder;
    }

    static Map<String, String> getMap(){
        return Collections.unmodifiableMap(plainConfigMap);
    }

    /**
     * replace the place holder
     * @param content
     * @param enableSubstitutionInVariables tell if replace a placeholder in another
     * @return
     */
    public static String replacePlaceHolder(String content, boolean enableSubstitutionInVariables){
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
        return replacePlaceHolder(content, false);
    }

    /**
     * build the whole config
     * @param builder
     * @param config
     */
    private static void buildConfig(Map<String, String> builder, Config config){
        Assert.notNull(builder, "the build cannot be null");

        Set<String> propertyNames = config.getPropertyNames();

        for (String propertyName :
                propertyNames) {
            if(builder.containsKey(propertyName)) throw new ApolloConfigException("apollo键值重复！");
            String value = config.getProperty(propertyName, "");
            builder.put(propertyName, value);
        }
    }

    /**
     * if the annotation is above a field
     * @param tClass
     * @param <T>
     * @return
     */
    private static <T> T buildFieldAnnotatedBean(Class<T> tClass){
        T result;
        try{
            result = tClass.newInstance();
        }catch (Exception e){
            throw new RuntimeException("cannot find a default construct to new instance");
        }

        Field[] declaredFields = tClass.getDeclaredFields();
        for (Field f :
                declaredFields) {
            ConfigKey fieldAnnotation = f.getAnnotation(ConfigKey.class);
            if(fieldAnnotation == null){
                continue;
            }
            String key = fieldAnnotation.value();
            if(StringUtils.isNotEmpty(key)){
                Class<?> type = f.getType();
                String value = getConfig(key);
                if(value != null){
                    Object parse = ParserFactory.getParser(type).parse(value, type);
                    f.setAccessible(true);
                    try {
                        f.set(result, parse);
                    } catch (IllegalAccessException e) {
                        logger.error("set value error", e);
                    }
                }
            }else{//if there is no keys, parse recursively
                Class<?> type = f.getType();
                Object config = getConfig(type);
                if(config != null){
                    f.setAccessible(true);
                    try {
                        f.set(result, config);
                    } catch (IllegalAccessException e) {
                        logger.error("set value error", e);
                    }
                }
            }


        }
        return result;
    }

    /**
     * if the annotation is above a class
     * @param configKey
     * @param tClass
     * @param <T>
     * @return
     */
    private static <T> T buildTypeAnnotatedBean(ConfigKey configKey, Class<T> tClass){
        String key = configKey.value();
        String value = getConfig(key);
        return (T) ParserFactory.getParser(tClass).parse(value, tClass);
    }


    /**
     * get a value by certain type
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T>  T getConfig(Class<T> tClass) {
        ConfigKey annotation = tClass.getAnnotation(ConfigKey.class);
        if(annotation == null){// not class level, recursively scan sub-field
            return buildFieldAnnotatedBean(tClass);
        }else{
            return buildTypeAnnotatedBean(annotation, tClass);
        }
    }

    /**
     * get the value from a config service.
     * if the namespace is set , the value will override the default namespace
     * this procedure is not reversible, which is to say. if you delete the value in the namespace, the framework would not
     * get the value by the same key from the default namespace
     * @param key
     * @return
     */
    private static String getConfig(String key){
        if(hasNameSpace){
            String property = nameSpaceConfigService.getProperty(key, null);
            if(property != null){
                return property;
            }
        }
        return configService.getProperty(key, null);
    }

    /**
     * create a listener
     */
    private static void startWatchThread(){
        if(!hasStartWatchThread.get()){
            if(!hasStartWatchThread.compareAndSet(false, true)) return;
        }

        if(hasNameSpace){
            nameSpaceConfigService.addChangeListener(configChangeEvent -> {
                Set<String> keys = configChangeEvent.changedKeys();
                for (String key :
                     keys) {
                    update(key, configChangeEvent.getChange(key));

                }
            });
        }

        configService.addChangeListener(configChangeEvent -> {
            Set<String> keys = configChangeEvent.changedKeys();
            for (String key :
                    keys) {
                update(key, configChangeEvent.getChange(key));
            }
        });
    }

    private static void update(String key, ConfigChange configChange){
        String value = configChange.getNewValue();
        plainConfigMap.put(key, value);
        WatchCache[] toBeUpdates = watchCacheMap.get(key);
        for (WatchCache wc :
                toBeUpdates) {
            update(wc, value);
        }

    }

    private static void update(WatchCache watchCache, String value){
        switch (watchCache.watchType){
            case TYPE: {
                Object parse = ParserFactory.getParser(watchCache.classType).parse(value, watchCache.classType);
                watchCache.consumer.accept(parse);
                break;
            }
            case FIELD: {
                Object parse = ParserFactory.getParser(watchCache.fieldClass).parse(value, watchCache.fieldClass);
                Object clonedObject = null;
                try {
                    clonedObject = BeanUtils.cloneBean(watchCache.originObject);
                } catch (Exception e) {
                    logger.error("error clone config object", e);
                    return;
                }
                watchCache.field.setAccessible(true);
                try {
                    watchCache.field.set(clonedObject, parse);
                } catch (IllegalAccessException e) {
                    logger.warn("set value failed", e);
                }
                watchCache.consumer.accept(clonedObject);
                watchCache.originObject = clonedObject;
                break;
            }
            case PLAIN://保留字段，以后实现运行时加载
            default:{
                watchCache.consumer.accept(value);
            }

        }

    }

    /**
     * create a watch, which can keep the value updated
     * @param tClass
     * @param consumer
     * @param <T>
     */
    public static <T> void watch(Class<T> tClass, Consumer<T> consumer){
        startWatchThread();
        ConfigKey annotation = tClass.getAnnotation(ConfigKey.class);
        if(annotation == null){// not class level, recursively scan sub-field
            T t = buildFieldAnnotatedBean(tClass);
            consumer.accept(t);
            loadOnFieldAnnotatedClass(tClass, t, consumer);
        }else{
            T t = buildTypeAnnotatedBean(annotation, tClass);
            consumer.accept(t);
            String key = annotation.value();
            buildWatchCache(key, new WatchCache(tClass, consumer));
        }
    }


    private static  <T>  void loadOnFieldAnnotatedClass(Class<T> tClass, T object, Consumer<T> consumer){
        Field[] declaredFields = tClass.getDeclaredFields();
        for (Field f :
                declaredFields) {
            ConfigKey fieldAnnotation = f.getAnnotation(ConfigKey.class);
            if(fieldAnnotation == null){
                continue;
            }
            String key = fieldAnnotation.value();

            Class<?> type = f.getType();

            if(consumer != null){//with consumer, put it it in a array to be updated
                buildWatchCache(key, new WatchCache(f, type, object, consumer));
            }
        }
    }


    private static void buildWatchCache(String key, WatchCache watchCache){
        synchronized (watchCacheMap){
            WatchCache[] watchCaches = watchCacheMap.get(key);
            if(watchCaches == null){
                watchCaches = new WatchCache[]{watchCache};
                watchCacheMap.put(key, watchCaches);
            }else{
                WatchCache[] newArray = new WatchCache[watchCaches.length + 1];
                System.arraycopy(watchCaches, 0, newArray, 0, watchCaches.length);
                newArray[watchCaches.length] = watchCache;
                watchCacheMap.put(key, newArray);

            }
        }
    }

    private enum WatchType{
        PLAIN,FIELD, TYPE
    }

    private static class WatchCache{
        private WatchType watchType;
        private Class<?> classType;
        private Consumer consumer;
        private Field field;
        private Class<?> fieldClass;
        private Object originObject;

        public WatchCache(Class<?> classType, Consumer consumer) {
            this.classType = classType;
            this.consumer = consumer;
            this.watchType = WatchType.TYPE;
        }

        public WatchCache(Field field, Class<?> fieldClass, Object originObject, Consumer consumer) {
            this.consumer = consumer;
            this.field = field;
            this.fieldClass = fieldClass;
            this.originObject = originObject;
            this.watchType = WatchType.FIELD;
        }

        public WatchCache(Consumer consumer){
            this.watchType = WatchType.PLAIN;
            this.consumer = consumer;
        }
    }
}
