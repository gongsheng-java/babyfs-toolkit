package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Double类型数据验证规则，提供最大值最小值验证
 * <p/>
 */
@XmlRootElement(name = "double")
public class DoubleRule implements IValidateRule {
    /**
     * 默认Integer数据最小值
     */
    public static final double DEFAULT_MIN_VALUE = Double.MIN_VALUE;
    /**
     * 默认Integer数据最大值
     */
    public static final double DEFAULT_MAX_VALUE = Double.MAX_VALUE;

    /**
     * Double数据最小值
     */
    @XmlElement
    private double minValue;
    /**
     * Double数据最大值
     */
    @XmlElement
    private double maxValue;

    /**
     * JAXB数据绑定使用
     */
    private DoubleRule() {
        minValue = DEFAULT_MIN_VALUE;
        maxValue = DEFAULT_MAX_VALUE;
    }

    /**
     * 构造方法
     *
     * @param minValue 最小值，为null则设置为{@link #DEFAULT_MIN_VALUE DEFAULT_MIN_VALUE}
     * @param maxValue 最大值，为null则设置为{@link #DEFAULT_MAX_VALUE DEFAULT_MAX_VALUE}
     */
    public DoubleRule(Double minValue, Double maxValue) {
        this.minValue = (minValue == null) ? DEFAULT_MIN_VALUE : minValue;
        this.maxValue = (maxValue == null) ? DEFAULT_MAX_VALUE : maxValue;
    }

    @Override
    public boolean validate(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }
        try {
            double v = Double.parseDouble(value);
            return !(v < minValue || v > maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getErrorMsg() {
        return "value is not a valid integer or out of range[" + minValue + "," + maxValue + "]";
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
}
