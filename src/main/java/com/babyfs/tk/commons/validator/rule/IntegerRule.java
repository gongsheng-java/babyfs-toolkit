package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Integer类型数据验证规则，提供最大值最小值验证
 * <p/>
 */
@XmlRootElement(name = "int")
public class IntegerRule implements IValidateRule {
    /**
     * 默认Integer数据最小值
     */
    public static final int DEFAULT_MIN_VALUE = Integer.MIN_VALUE;
    /**
     * 默认Integer数据最大值
     */
    public static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;

    /**
     * Integer数据最小值
     */
    @XmlElement
    private int minValue;
    /**
     * Integer数据最大值
     */
    @XmlElement
    private int maxValue;

    /**
     * JAXB数据绑定使用
     */
    private IntegerRule() {
        minValue = DEFAULT_MIN_VALUE;
        maxValue = DEFAULT_MAX_VALUE;
    }

    /**
     * 构造方法
     *
     * @param minValue 最小值，为null则设置为{@link #DEFAULT_MIN_VALUE DEFAULT_MIN_VALUE}
     * @param maxValue 最大值，为null则设置为{@link #DEFAULT_MAX_VALUE DEFAULT_MAX_VALUE}
     */
    public IntegerRule(Integer minValue, Integer maxValue) {
        this.minValue = (minValue == null) ? DEFAULT_MIN_VALUE : minValue;
        this.maxValue = (maxValue == null) ? DEFAULT_MAX_VALUE : maxValue;
    }

    @Override
    public boolean validate(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }
        try {
            int v = Integer.parseInt(value);
            return !(v < minValue || v > maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getErrorMsg() {
        return String.format("value is not a valid integer or out of range[%d, %d]", minValue, maxValue);
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }
}
