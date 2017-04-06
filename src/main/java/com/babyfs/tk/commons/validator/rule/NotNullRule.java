package com.babyfs.tk.commons.validator.rule;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 非null规则
 * <p/>
 */
@XmlRootElement(name = "notNull")
public class NotNullRule implements IValidateRule {
    @Override
    public boolean validate(String value) {
        return value != null;
    }

    @Override
    public String getErrorMsg() {
        return "value is null";
    }
}
