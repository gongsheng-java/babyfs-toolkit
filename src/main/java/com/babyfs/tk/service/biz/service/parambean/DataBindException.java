package com.babyfs.tk.service.biz.service.parambean;

/**
 * 数据绑定异常，在数据校验或类型转换出错时，抛出该异常
 * <p/>
 */
public class DataBindException extends Exception {
    /**
     * 发生异常的字段名称
     */
    private final String fieldName;

    public DataBindException(String fieldName) {
        this.fieldName = fieldName;
    }

    public DataBindException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public DataBindException(String fieldName, String message, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }

    public DataBindException(String fieldName, Throwable cause) {
        super(cause);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return String.format("%s: Error field: %s, Error Message: %s", this.getClass().getName(), fieldName, getMessage());
    }
}
