package com.babyfs.tk.service.biz.kvconf;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * {@link KVConfType#JSONOBJECT_TEXT}的类型定义
 */
public interface KVConfJSONType {
    /**
     * 系统配置前缀
     */
    String SYS_CONF_PRFIX = "_sys.";
    /**
     * 系统配置子key分隔
     */
    String SYS_CONF_SUB_SEP = "_sub_";

    /**
     * KV Key
     *
     * @return
     */
    String getKey();

    /**
     * 构建子key
     *
     * @param id
     * @return
     */
    String buildSubKey(Object id);

    /**
     * KV Value的类型,用于解析为对应的对象
     *
     * @return
     */
    Class getValueType();

    /**
     * 是否是系统内置的配置
     *
     * @return
     */
    boolean isSys();


    /**
     * 生成系统内置名称
     *
     * @param name
     * @return
     */
    static String sysName(String name) {
        Preconditions.checkNotNull(name);
        return (SYS_CONF_PRFIX + name).toLowerCase();
    }

    /**
     * 是否是系统内置名称
     *
     * @param name
     * @return
     */
    static boolean isSysName(String name) {
        return !Strings.isNullOrEmpty(name) && name.startsWith(SYS_CONF_PRFIX);
    }

    static void register(String key, KVConfJSONType type) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(type);
        TypeReg.keyToType.put(key, type);
    }

    static KVConfJSONType get(String key) {
        if (key == null) {
            return null;
        }
        KVConfJSONType jsonType = TypeReg.keyToType.get(key);
        if (jsonType != null) {
            return jsonType;
        }

        //是子类型的key
        if (key.startsWith(SYS_CONF_PRFIX) && key.contains(SYS_CONF_SUB_SEP)) {
            String primaryKey = key.substring(0, key.indexOf(SYS_CONF_SUB_SEP));
            return TypeReg.keyToType.get(primaryKey);
        }
        return null;
    }

    class TypeReg {
        private static final Map<String, KVConfJSONType> keyToType = Maps.newHashMap();

    }
}
