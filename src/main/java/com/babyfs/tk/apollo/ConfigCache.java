package com.babyfs.tk.apollo;


import com.babyfs.tk.apollo.annotation.ConfigKey;
import com.babyfs.tk.apollo.module.WatchCacheNode;
import com.babyfs.tk.apollo.parser.ParserFactory;
import com.babyfs.tk.commons.service.GuiceInjector;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConfigCache {

    private static Logger logger = LoggerFactory.getLogger(ConfigCache.class);


    /**
     * 一个key 可以有一个watch cache node
     */
    private Map<String, WatchCacheNode[]> watchCacheNodeMap = Maps.newHashMap();

    /**
     * 是否已经加上watch listener
     */
    private AtomicBoolean hasStartWatchThread = new AtomicBoolean(false);

    /**
     * 对应namespace 的 config
     */
    private Config config;

    /**
     * 是否启用dev 覆盖机制
     */
    private boolean isDev = false;

    private Map<String, String> localConfig;

    public void setDev(Map<String,String> localConfig){
        if(CollectionUtils.isEmpty(localConfig)){
            return;
        }

        this.localConfig = localConfig;
        this.isDev = true;
    }

    private void override(Map<String, String> map){
        if(!isDev) return;

        Set<Map.Entry<String, String>> entries = this.localConfig.entrySet();
        for (Map.Entry<String, String> entry :
                entries) {
            map.put(entry.getKey(), entry.getValue());
        }
    }

    private String getDevValue(String key){
        if(!isDev) return null;
        return localConfig.get(key);
    }

    public ConfigCache(String namespace){
        this.config = ConfigService.getConfig(namespace);
    }

    public ConfigCache(){
        this.config = ConfigService.getAppConfig();
    }

    public Map<String, String> getConfigMap(){
        Map<String, String> builder = Maps.newHashMap();

        Set<String> propertyNames = config.getPropertyNames();

        for (String propertyName :
                propertyNames) {
            if(builder.containsKey(propertyName)) throw new ApolloConfigException("apollo键值重复！");
            String value = config.getProperty(propertyName, "");
            builder.put(propertyName, value);
        }

        override(builder);
        return Collections.unmodifiableMap(builder);
    }

    /**
     * create a listener
     */
    private void startWatchThread(){
        if(!hasStartWatchThread.get()){
            if(!hasStartWatchThread.compareAndSet(false, true)) return;
            config.addChangeListener(configChangeEvent -> {
                Set<String> keys = configChangeEvent.changedKeys();
                for (String key :
                        keys) {
                    if(getDevValue(key) != null){
                        logger.info("local map contains key, ignore it");
                        continue;
                    }
                    update(key, configChangeEvent.getChange(key));
                }
            });
        }
    }



    private void update(String key, ConfigChange configChange){
        String value = configChange.getNewValue();
        WatchCacheNode[] toBeUpdates = watchCacheNodeMap.get(key);
        for (WatchCacheNode wc :
                toBeUpdates) {
            update(wc, value);
        }

    }

    private void update(WatchCacheNode watchCache, String value){
        switch (watchCache.getWatchType()){
            case TYPE: {
                Object parse = ParserFactory.getParser(watchCache.getClassType()).parse(value, watchCache.getClassType());
                watchCache.getConsumer().accept(parse);
                break;
            }
            case FIELD: {
                Object parse = ParserFactory.getParser(watchCache.getFieldClass()).parse(value, watchCache.getFieldClass());
                Object clonedObject = null;
                try {
                    clonedObject = BeanUtils.cloneBean(watchCache.getOriginObject());
                } catch (Exception e) {
                    logger.error("error clone config object", e);
                    return;
                }
                watchCache.getField().setAccessible(true);
                try {
                    watchCache.getField().set(clonedObject, parse);
                } catch (IllegalAccessException e) {
                    logger.warn("set value failed", e);
                }
                watchCache.getConsumer().accept(clonedObject);
                watchCache.setOriginObject(clonedObject);
                break;
            }
            case PLAIN://保留字段，以后实现运行时加载
            default:{
                watchCache.getConsumer().accept(value);
            }

        }

    }

    /**
     * create a watch, which can keep the value updated
     * @param tClass
     * @param consumer
     * @param <T>
     */
    public <T> void watch(Class<T> tClass, Consumer<T> consumer){
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
            buildWatchCache(key, new WatchCacheNode(tClass, consumer));
        }
    }


    private  <T>  void loadOnFieldAnnotatedClass(Class<T> tClass, T object, Consumer<T> consumer){
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
                buildWatchCache(key, new WatchCacheNode(f, type, object, consumer));
            }
        }
    }


    private void buildWatchCache(String key, WatchCacheNode watchCache){
        synchronized (watchCacheNodeMap){
            WatchCacheNode[] watchCaches = watchCacheNodeMap.get(key);
            if(watchCaches == null){
                watchCaches = new WatchCacheNode[]{watchCache};
                watchCacheNodeMap.put(key, watchCaches);
            }else{
                WatchCacheNode[] newArray = new WatchCacheNode[watchCaches.length + 1];
                System.arraycopy(watchCaches, 0, newArray, 0, watchCaches.length);
                newArray[watchCaches.length] = watchCache;
                watchCacheNodeMap.put(key, newArray);

            }
        }
    }

    /**
     * if the annotation is above a field
     * @param tClass
     * @param <T>
     * @return
     */
    private <T> T buildFieldAnnotatedBean(Class<T> tClass){
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
    private <T> T buildTypeAnnotatedBean(ConfigKey configKey, Class<T> tClass){
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
    public <T>  T getConfig(Class<T> tClass) {
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
    public String getConfig(String key){
        String result;
        if((result = getDevValue(key)) != null){
            return result;
        }
        return config.getProperty(key, null);
    }

}
