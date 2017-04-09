package com.babyfs.tk.service.biz.constants;

/**
 * 基础的ES索引字段,名称来自实体由{@link javax.persistence.Column}定义的名称,即与数据库表字段保持一致
 */
public abstract class BaseESFields {
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String STAT = "stat";
    public static final String START = "start";
    public static final String END = "end";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String LOCATION = "location";
    public static final String DATE_TIME = "date_time";
    public static final String SCORE = "_score";
    public static final String PARENT = "_parent";

    protected BaseESFields() {

    }
}
