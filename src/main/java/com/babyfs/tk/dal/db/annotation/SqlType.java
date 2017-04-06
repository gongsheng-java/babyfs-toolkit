package com.babyfs.tk.dal.db.annotation;

/**
 * 查询的类型
 */
public enum SqlType {
    /**
     * 查询实体
     */
    QUERY_ENTITY,
    /**
     * 查询指定的列
     */
    QUERY_COLUMNS,
    /**
     * 查询个数
     */
    QUERY_COUNT,
    /**
     * 更新指定的列
     */
    UPDATE_COLUMNS,
    /**
     * 删除语句
     */
    DELETE,
    /**
     * 更新实体
     */
    UPDATE_ENTITY,
    /**
     * 自定义的更新
     */
    EXEC;
}
