package com.babyfs.tk.commons.validator.jaxb;

import com.babyfs.tk.commons.validator.RuleChain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * JAXB数据绑定使用，用于包装XML根节点
 * <p/>
 */
@XmlRootElement(name = "rules")
@XmlAccessorType(XmlAccessType.FIELD)
public final class RuleChainList {
    /**
     * 规则list
     */
    @XmlElement(name = "rule", required = true)
    private List<RuleChain> rules;

    /**
     * 构造方法，JAXB创建对象实例时使用
     */
    public RuleChainList() {
    }

    public List<RuleChain> getRules() {
        return rules;
    }
}
