package com.babyfs.tk.dal.db.annotation;

import com.babyfs.tk.dal.orm.IEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dao的描述
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dao {
    /**
     * Dao所操作的对象类型
     *
     * @return
     */
    Class<? extends IEntity> entityClass();
}
