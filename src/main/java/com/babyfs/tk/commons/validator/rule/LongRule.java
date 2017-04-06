package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Long类型数据验证规则，提供最大值最小值验证
 * <p/>
 */
@XmlRootElement(name = "long")
public class LongRule implements IValidateRule {
    /**
     * 默认Integer数据最小值
     */
    private static final long DEFAULT_MIN_VALUE = Long.MIN_VALUE;
    /**
     * 默认Integer数据最大值
     */
    private static final long DEFAULT_MAX_VALUE = Long.MAX_VALUE;

    /**
     * Long数据最小值
     */
    private long minValue;
    /**
     * Long数据最大值
     */
    private long maxValue;


    /**
     * JAXB数据绑定使用
     */
    private LongRule() {
        minValue = DEFAULT_MIN_VALUE;
        maxValue = DEFAULT_MAX_VALUE;
    }

    /**
     * 验证value的值是否合法
     *
     * @param value 需要验证的值
     * @return true表示验证成功，false表示失败
     */
    @Override
    public boolean validate(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }
        try {
            long v = Long.parseLong(value);
            return !(v < minValue || v > maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 返回验证错误信息
     *
     * @return
     */
    @Override
    public String getErrorMsg() {
        return String.format("value is not a valid long or out of range[%d, %d]", minValue, maxValue);
    }

    @XmlAttribute
    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    @XmlAttribute
    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
}
