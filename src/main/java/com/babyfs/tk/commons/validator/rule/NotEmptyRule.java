package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 非空值验证规则，不能为null和空串
 * <p/>
 */
@XmlRootElement(name = "notEmpty")
public class NotEmptyRule implements IValidateRule {

    @Override
    public boolean validate(String value) {
        return !Strings.isNullOrEmpty(value);
    }

    @Override
    public String getErrorMsg() {
        return "value can not be null or empty";
    }
}
