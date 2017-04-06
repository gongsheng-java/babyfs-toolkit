package com.babyfs.tk.service.biz.service.parambean;

/**
 * 数据类型转换接口定义
 * <p/>
 */
public interface ITypeConverter {
    /**
     * 将传入的value转换成由targetType指定的类型
     *
     * @param targetType 需要转换成的目标类型
     * @param value      需要转换的值
     * @return 转换完成之后的值
     */
    <T> T convert(Class<T> targetType, String value);
}
