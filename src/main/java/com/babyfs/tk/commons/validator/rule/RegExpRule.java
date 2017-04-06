package com.babyfs.tk.commons.validator.rule;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.regex.Pattern;

/**
 * 正则表达式验证规则
 * <p/>
 */
@XmlRootElement(name = "regExp")
public class RegExpRule implements IValidateRule {
    /**
     * 编译好的正则表达式模式，可在规则创建时即可检测出正则表达式的正确性
     */
    @XmlValue
    @XmlJavaTypeAdapter(PatternAdaptor.class)
    private Pattern pattern;

    /**
     * JAXB数据绑定使用
     */
    private RegExpRule() {
    }

    /**
     * 构造函数
     *
     * @param regExp 用于校验的正则表达式，错误的正则表达式会导致对象创建失败
     */
    public RegExpRule(String regExp) {
        this.pattern = Pattern.compile(regExp);
    }

    @Override
    public boolean validate(String value) {
        return value == null || pattern.matcher(value).matches();
    }

    @Override
    public String getErrorMsg() {
        return String.format("value does not match the pattern '%s'", pattern.pattern());
    }


    public static class PatternAdaptor extends XmlAdapter<String, Pattern> {

        @Override
        public Pattern unmarshal(String v) throws Exception {
            try {
                return Pattern.compile(v);
            } catch (Exception e) {
                throw new Exception("Compile pattern fail:"+v,e);
            }
        }

        @Override
        public String marshal(Pattern v) throws Exception {
            return v.pattern();
        }
    }
}
