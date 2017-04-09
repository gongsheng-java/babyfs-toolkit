package com.babyfs.tk.service.biz.base.es.json;

import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.babyfs.tk.dal.DalUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.base.Tuple;
import com.babyfs.tk.dal.orm.IEntity;

import javax.persistence.Column;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * fastjson filter builder
 */
public class JSONFilterBuilder {
    /**
     * 所有的过滤器
     */
    private List<SerializeFilter> allFilters = Lists.newArrayList();

    /**
     * 增加entityClass属性名称替换NameFilter,构建规则:
     * <p>使用{@link NameFilter}对实体的属性名进行改写,优先使用{@link IEntity}使用Column声明的名称,如果没有Column则忽略属性</p>
     * <p>如果excludeAttributes不为空,则使用{@link SimplePropertyPreFilter}进行过滤</p>
     *
     * @param entityClass       实体类
     * @param excludeAttributes 需要被删除的属性,该属性是Java Bean属性
     * @param <T>               实体的类型
     * @return self
     */
    public <T extends IEntity> JSONFilterBuilder entityNameFilter(Class<T> entityClass, String... excludeAttributes) {
        Preconditions.checkNotNull(entityClass);
        Method[] declaredMethods = entityClass.getMethods();

        Map<String, String> attributeColumnMap = Maps.newHashMap();
        List<String> excludeAttributeList = Lists.newArrayList();

        if (excludeAttributes != null) {
            for (String attribute : excludeAttributes) {
                if (!Strings.isNullOrEmpty(attribute)) {
                    excludeAttributeList.add(attribute);
                }
            }
        }

        excludeAttributeList.add("ver");

        for (Method method : declaredMethods) {
            Column column = method.getAnnotation(Column.class);
            final String name = method.getName();

            if (column == null) {
                if (name.startsWith("get")) {
                    final String attribueName = DalUtil.toLower(name.substring("get".length()));
                    excludeAttributeList.add(attribueName);
                }
                continue;
            }

            final String columnName = column.name();
            Preconditions.checkState(!Strings.isNullOrEmpty(columnName), "The column name of the method " + name + "must be set ");
            Preconditions.checkState(name.startsWith("get"), "The method " + name + " is not a getter method.The @Column method name must start with 'get'.");
            final String attribueName = name.substring("get".length());
            Preconditions.checkState(!Strings.isNullOrEmpty(attribueName), "Can't find the attribute name from the method " + name + ".");
            String normalizeName = DalUtil.toLower(attribueName);
            if (!normalizeName.equals(columnName)) {
                attributeColumnMap.put(normalizeName, columnName);
            }
        }

        List<SerializeFilter> serializeFilters = Lists.newArrayList();
        if (!attributeColumnMap.isEmpty()) {
            serializeFilters.add(new NameReplaceFileter(entityClass, attributeColumnMap));
        }

        if (!excludeAttributeList.isEmpty()) {
            SimplePropertyPreFilter propertyPreFilter = new SimplePropertyPreFilter(entityClass);
            propertyPreFilter.getExcludes().addAll(excludeAttributeList);
            serializeFilters.add(propertyPreFilter);
        }

        this.allFilters.addAll(serializeFilters);
        return this;
    }

    /**
     * 添加值修改函数
     *
     * @param clazz not null
     * @param funcs 属性修改函数,key为属性名,value是修改函数,函数的参数格式符合{@link com.alibaba.fastjson.serializer.ValueFilter#process(Object, String, Object)}一致
     * @param <T>   对象的类型
     * @return self
     */
    public <T> JSONFilterBuilder valueFilter(Class<T> clazz, Map<String, Function<Tuple<T, String, Object>, Object>> funcs) {
        this.filter(new ValueReplaceFilter<>(clazz, funcs));
        return this;
    }

    /**
     * 添加值增加函数
     *
     * @param clazz not null
     * @param func  属性增加函数,func的参数是clazz的实例,返回结果是需要新增加的属性列表
     *              func的返回值如果都是简单属性,可以一次返回多个简单属性;
     *              如果返回值是复合对象,则只返回一个值,即复合对象,应该有单独的ValueAddAfterFiler,否则会导致fastjson序列化时抛出NPE
     * @param <T>   对象的类型
     * @return
     * @see {@link ValueAddAfterFilter#ValueAddAfterFilter(Class, Function)}
     */
    public <T> JSONFilterBuilder afterValueFilter(Class<T> clazz, Function<T, List<Pair<String, Object>>> func) {
        this.filter(new ValueAddAfterFilter<>(clazz, func));
        return this;
    }

    /**
     * 添加一个Filter
     *
     * @param filter not null
     * @return self
     */
    public JSONFilterBuilder filter(SerializeFilter filter) {
        Preconditions.checkNotNull(filter);
        this.allFilters.add(filter);
        return this;
    }

    /**
     * 添加n个Filter
     *
     * @param filters
     * @return self
     */
    public JSONFilterBuilder filters(SerializeFilter... filters) {
        if (filters == null || filters.length == 0) {
            return this;
        }
        Collections.addAll(this.allFilters, filters);
        return this;
    }


    /**
     * 构建结果
     *
     * @return array
     */
    public SerializeFilter[] build() {
        return toArray(this.allFilters);
    }


    /**
     * 转换为数组
     *
     * @param serializeFilters list fo {@link SerializeFilter}
     * @return not null array
     */
    public static SerializeFilter[] toArray(List<SerializeFilter> serializeFilters) {
        if (serializeFilters == null || serializeFilters.isEmpty()) {
            return new SerializeFilter[0];
        } else {
            SerializeFilter[] ret = new SerializeFilter[serializeFilters.size()];
            return serializeFilters.toArray(ret);
        }
    }
}
