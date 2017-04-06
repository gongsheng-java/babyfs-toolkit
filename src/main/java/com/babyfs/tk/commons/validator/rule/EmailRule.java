package com.babyfs.tk.commons.validator.rule;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Pattern;

/**
 * Email验证规则
 * <p/>
 */
@XmlRootElement(name = "email")
public final class EmailRule implements IValidateRule {
    private static final String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]";
    private static final String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
    private static final String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

    private static Pattern pattern = Pattern.compile(
            "^" + ATOM + "+(\\." + ATOM + "+)*@"
                    + DOMAIN
                    + "|"
                    + IP_DOMAIN
                    + ")$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean validate(String value) {
        return Strings.isNullOrEmpty(value) || pattern.matcher(value).matches();
    }

    @Override
    public String getErrorMsg() {
        return "not a valid email";
    }
}
