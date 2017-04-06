package com.babyfs.tk.commons.validator.rule;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 字符串验证规则，提供最小长度和最大长度验证
 * <p/>
 */
@XmlRootElement(name = "string")
public class StringRule implements IValidateRule {
    /**
     * 默认字符串最小长度
     */
    public static final int DEFAULT_MIN_LENGTH = 0;
    /**
     * 默认字符串最大长度
     */
    public static final int DEFAULT_MAX_LENGTH = Integer.MAX_VALUE;

    /**
     * 字符串最小长度
     */
    @XmlElement
    private int minLength;
    /**
     * 字符串最大长度
     */
    @XmlElement
    private int maxLength;

    /**
     * JAXB数据绑定使用
     */
    private StringRule() {
        minLength = DEFAULT_MIN_LENGTH;
        maxLength = DEFAULT_MAX_LENGTH;
    }

    /**
     * 构造函数
     *
     * @param minLength 字符串最小长度，为null则设置为{@link #DEFAULT_MIN_LENGTH DEFAULT_MIN_LENGTH}
     * @param maxLength 字符串最大长度，为null则设置为{@link #DEFAULT_MAX_LENGTH DEFAULT_MAX_LENGTH}
     */
    public StringRule(Integer minLength, Integer maxLength) {
        this.minLength = (minLength == null) ? DEFAULT_MIN_LENGTH : minLength;
        this.maxLength = (maxLength == null) ? DEFAULT_MAX_LENGTH : maxLength;
    }

    @Override
    public boolean validate(String value) {
        if (value == null) {
            return true;
        }
        int l = value.length();
        return !(l < minLength || l > maxLength);
    }

    @Override
    public String getErrorMsg() {
        return String.format("value length is out of range [%d,%d]", minLength, maxLength);
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
