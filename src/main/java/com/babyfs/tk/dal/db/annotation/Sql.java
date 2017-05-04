package com.babyfs.tk.dal.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询类实体型的标注
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {
    /**
     * 查询的类型
     *
     * @return
     */
    SqlType type() default SqlType.QUERY_ENTITY;

    /**
     * 查询的列,当{@link #type()}的类型为{@link SqlType#QUERY_COLUMNS}时使用该属性指定列
     *
     * @return
     */
    String columns() default "";

    /**
     * condition  查询的条件
     *
     * @return
     */
    String condition();

    /**
     * {@link SqlType#EXEC} 执行的语句
     *
     * @return
     */
    String execSql() default "";

    /**
     * 是否替换{@link SqlType#EXEC} 语句中的表名,如果为true,则{@link #execSql()}语句中的表名应该用%s代替;否则使用固定表名
     *
     * @return
     */
    boolean replaceTableName() default true;

    /**
     * 供{@link SqlType#UPDATE_PARTIAL_ENTITY}使用,表示更新实体是需要包含的字段
     *
     * @return
     */
    String[] includeColumns() default {};

    /**
     * 供{@link SqlType#UPDATE_PARTIAL_ENTITY}使用,表示更新实体是需要排除的字段
     *
     * @return
     */
    String[] excludeColumns() default {};
}
