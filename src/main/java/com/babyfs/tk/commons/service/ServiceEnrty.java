package com.babyfs.tk.commons.service;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 服务实体
 */
public final class ServiceEnrty {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEnrty.class);

    public static final TypeLiteral<Set<ServiceEnrty>> SET_TYPE_LITERAL = (TypeLiteral<Set<ServiceEnrty>>) TypeLiteral.get(Types.setOf(ServiceEnrty.class));
    public static final Key<Set<ServiceEnrty>> SET_KEY = Key.get(SET_TYPE_LITERAL);

    /**
     * 服务名：默认为接口的SimpleName, 如果是基于 annotion 的，则默认是 annotatedName
     */
    private final String name;
    /**
     * 服务接口类名：
     */
    private final String className;
    /**
     * Guice的Key
     */
    private final Key<?> guiceKey;
    /**
     * 基于annotion Named 绑定的名字，默认为null
     */
    private final String annotatedName;


    public ServiceEnrty(String name, String className, Key<?> guiceKey, String annotatedName) {
        this.name = name;
        this.className = className;
        this.guiceKey = guiceKey;
        this.annotatedName = annotatedName;
    }

    /**
     * 取得所有的ServiceEntry定义
     *
     * @param injector
     * @return
     */
    public static Set<ServiceEnrty> getAllServices(Injector injector) {
        List<Binding<Set<ServiceEnrty>>> bindingsByType = injector.findBindingsByType(SET_TYPE_LITERAL);
        if (bindingsByType == null || bindingsByType.isEmpty()) {
            LOGGER.warn("Can't find the defined ServiceEntry.");
            return Collections.emptySet();
        }
        return injector.getInstance(SET_KEY);
    }

    public String getAnnotatedName() {
        return annotatedName;
    }

    public String getName() {
        return name;
    }

    public Key<?> getGuiceKey() {
        return guiceKey;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceEnrty that = (ServiceEnrty) o;

        if (!guiceKey.equals(that.guiceKey)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + guiceKey.hashCode();
        return result;
    }
}
