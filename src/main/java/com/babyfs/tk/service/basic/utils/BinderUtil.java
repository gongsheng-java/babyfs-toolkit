package com.babyfs.tk.service.basic.utils;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Guice绑定工具类
 */
public final class BinderUtil {
    private BinderUtil() {

    }

    /**
     * key:queue name,value:Message Processor
     *
     * @param binder
     * @return
     */
    public static Multibinder<String> createSetBinder(@Nonnull Binder binder, @Nonnull String names) {
        return Multibinder.newSetBinder(binder, String.class, Names.named(names));
    }

    /**
     * 注册binder到map中
     *
     * @param binder
     */
    public static void addBinding(@Nonnull Multibinder<String> binder, @Nonnull String name) {
        binder.addBinding().toInstance(name);
    }

    /**
     * 注册binder
     *
     * @param binder
     * @param name
     * @return
     */
    public static final MapBinder<String, Object> createMapBinder(@Nonnull Binder binder, @Nonnull String name) {
        return MapBinder.newMapBinder(binder, String.class, Object.class, Names.named(name));
    }

    /**
     * 注册binder
     *
     * @param binder
     * @param key
     * @param implementation
     */
    public static void addBinding(@Nonnull MapBinder<String, Object> binder, @Nonnull String key, @Nonnull Class<? extends Object> implementation) {
        binder.addBinding(key).to(implementation);
    }

    /**
     * 绑定命名的String实例
     *
     * @param binder
     * @param name
     * @param value
     */
    public static void bind(Binder binder, String name, String value) {
        bind(binder, String.class, name, value);
    }

    /**
     * 绑定命名的int实例
     *
     * @param binder
     * @param name
     * @param value
     */
    public static void bind(Binder binder, String name, int value) {
        bind(binder, Integer.class, name, value);
    }

    /**
     * 绑定命名的long实例
     *
     * @param binder
     * @param name
     * @param value
     */
    public static void bind(Binder binder, String name, long value) {
        bind(binder, Long.class, name, value);
    }


    /**
     * 绑定命名实例
     *
     * @param binder
     * @param clazz
     * @param name
     * @param value
     * @param <T>
     * @param <V>
     */
    public static <T, V extends T> void bind(Binder binder, Class<T> clazz, String name, V value) {
        binder.bind(clazz).annotatedWith(Names.named(name)).toInstance(value);
    }

    /**
     * 安装Modules
     *
     * @param binder
     * @param modules
     */
    public static void installModules(Binder binder, List<? extends Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return;
        }
        for (Module module : modules) {
            binder.install(module);
        }
    }
}
