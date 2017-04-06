package com.babyfs.tk.commons.validator.rule;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 布尔值验证规则
 * <p/>
 */
@XmlRootElement(name = "boolean")
public final class BooleanRule implements IValidateRule {

    @Override
    public boolean validate(String value) {
        if (value.equals(Boolean.TRUE.toString()) || value.equals(Boolean.FALSE.toString())) {
            return true;
        }
        return false;
    }

    @Override
    public String getErrorMsg() {
        return "not a valid boolean";
    }

}
