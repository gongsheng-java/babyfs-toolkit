package com.babyfs.tk.commons.validator;

import com.google.common.collect.ImmutableList;
import com.babyfs.tk.commons.validator.rule.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 验证规则链
 * <p/>
 */
@XmlRootElement(name = "rule")
public class RuleChain {
    @XmlElements({
            @XmlElement(name = "notEmpty", type = NotEmptyRule.class),
            @XmlElement(name = "email", type = EmailRule.class),
            @XmlElement(name = "string", type = StringRule.class),
            @XmlElement(name = "int", type = IntegerRule.class),
            @XmlElement(name = "long", type = LongRule.class),
            @XmlElement(name = "double", type = DoubleRule.class),
            @XmlElement(name = "notNull", type = NotNullRule.class),
            @XmlElement(name = "regExp", type = RegExpRule.class),
            @XmlElement(name = "pwd", type = PwdRule.class),
            @XmlElement(name = "uuid32", type = Uuid32Rule.class),
            @XmlElement(name = "boolean", type = BooleanRule.class)
    })
    private List<IValidateRule> ruleList;

    /**
     * 规则的名称
     */
    @XmlAttribute
    private String name;

    /**
     * 规则的描述
     */
    @XmlAttribute
    private String msg;

    /**
     * 是否按照or的模式验证,如果or为true,则所有的规则只要有一条通过即认为验证通过,默认为false
     */
    @XmlAttribute
    private boolean or = false;

    /**
     * JAXB数据绑定使用
     */
    private RuleChain() {
    }

    public RuleChain(String name, IValidateRule... rules) {
        this(name, null, rules);
    }

    public RuleChain(String name, String msg, IValidateRule... rules) {
        this.name = name;
        this.msg = msg;
        ruleList = ImmutableList.copyOf(rules);
    }

    public String getName() {
        return name;
    }

    public boolean isOr() {
        return or;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 顺序执行所有加入的规则，如果某个规则验证失败则直接返回
     *
     * @param value 需要验证的值
     * @return 规则验证结果
     */
    public ValidateResult validate(String value) {
        if (!or) {
            //and 全部通过
            for (IValidateRule rule : ruleList) {
                boolean res = rule.validate(value);
                if (!res) {
                    return new ValidateResult(false, msg);
                }
            }
            return ValidateResult.RESULT_OK;
        } else {
            // or 有一个通过
            for (IValidateRule rule : ruleList) {
                boolean res = rule.validate(value);
                if (res) {
                    return ValidateResult.RESULT_OK;
                }
            }
            return new ValidateResult(false, msg);
        }
    }
}
