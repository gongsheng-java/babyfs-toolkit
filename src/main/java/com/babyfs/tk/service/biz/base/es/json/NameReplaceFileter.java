package com.babyfs.tk.service.biz.base.es.json;

import com.alibaba.fastjson.serializer.NameFilter;

import java.util.Collections;
import java.util.Map;

/**
 * 名称替换
 */
public class NameReplaceFileter implements NameFilter {
    private final Map<String, String> attributeRenameMap;
    private final Class clazz;

    /**
     * @param clazz              可以为空
     * @param attributeRenameMap 可以为空
     */
    public NameReplaceFileter(Class clazz, Map<String, String> attributeRenameMap) {
        this.clazz = clazz;
        if (attributeRenameMap != null) {
            this.attributeRenameMap = attributeRenameMap;
        } else {
            this.attributeRenameMap = Collections.emptyMap();
        }
    }

    @Override
    public String process(Object object, String name, Object value) {
        if (clazz != null && !clazz.isInstance(object)) {
            return name;
        }
        if (name == null || name.length() == 0) {
            return name;
        }
        String replaceName = this.attributeRenameMap.get(name);
        if (replaceName != null) {
            return replaceName;
        } else {
            return name;
        }
    }
}
