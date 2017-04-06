package com.babyfs.tk.commons.config;

import java.util.Map;

/**
 * 配置服务,支持的方法有:
 * <ul>
 * <li>
 * {@link Map#get(Object)}
 * </li>
 * <li>
 * {@link Map#containsKey(Object)}
 * </li>
 * </ul>
 * 调用其他的Map接口方法会抛出{@link UnsupportedOperationException}
 */
public interface IConfigService extends Map<String, String> {
    /**
     * 加载配置
     */
    void load();

    /**
     * @param key
     * @return
     */
    String get(String key);

    /**
     *
     */
    String get(String key, String defaultValue);

    /**
     * @param key
     * @return
     */
    boolean containsKey(String key);
}
