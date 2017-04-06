package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Pattern;

/**
 * 密码验证规则
 * <p/>
 */
@XmlRootElement(name = "pwd")
public final class PwdRule implements IValidateRule {

    /**
     * 密码正则   TODO 详细规则待二期完善
     */
    private static final String REGEX = "^[\\dA-Za-z(!@#$%&)]{4,15}$";

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
        return "not a valid pwd";
    }

}
