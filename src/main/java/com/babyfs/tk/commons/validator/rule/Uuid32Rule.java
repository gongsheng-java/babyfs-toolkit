package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Pattern;

/**
 * 32位UUID验证规则
 * <p/>
 */
@XmlRootElement(name = "uuid32")
public final class Uuid32Rule implements IValidateRule {

    /**
     * 32位UUID正则
     */
    private static final String REGEX = "^([0-9]|[a-zA-Z]){32}$";

    /**
     * 正则模式
     */
    private static Pattern pattern = Pattern.compile(REGEX);

    @Override
    public boolean validate(String value) {
        return Strings.isNullOrEmpty(value) || pattern.matcher(value).matches();
    }

    @Override
    public String getErrorMsg() {
        return "not a valid UUID";
    }

}
