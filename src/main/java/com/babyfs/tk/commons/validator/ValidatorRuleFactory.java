package com.babyfs.tk.commons.validator;

import com.babyfs.tk.commons.validator.rule.*;
import com.babyfs.tk.commons.validator.jaxb.RuleChainList;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证规则的工厂类，提供常见规则的创建
 * <p/>
 */
public final class ValidatorRuleFactory {
    /**
     * 私有构造方法，阻止创建实例
     */
    private ValidatorRuleFactory() {

    }

    /**
     * 创建用于验证非负整数的验证规则
     *
     * @param required 是否为必填字段
     * @return
     */
    public static RuleChain createNonNegativeIntRule(boolean required) {
        return createIntRangeRule(required, 0, null);
    }

    /**
     * 创建验证整数范围[minValue, maxValue]的规则
     *
     * @param required 是否为必填字段
     * @param minValue 最小边界，包含，为null时使用{@link IntegerRule#DEFAULT_MIN_VALUE IntegerRule.DEFAULT_MIN_VALUE}
     * @param maxValue 最大边界，包含，为null时使用{@link IntegerRule#DEFAULT_MAX_VALUE IntegerRule.DEFAULT_MAX_VALUE}
     * @return
     */
    public static RuleChain createIntRangeRule(boolean required, Integer minValue, Integer maxValue) {
        if (required) {
            return new RuleChain("intRange", new NotEmptyRule(), new IntegerRule(minValue, maxValue));
        }
        return new RuleChain("optionalIntRange", new IntegerRule(minValue, maxValue));
    }

    /**
     * 创建验证Double范围[minValue, maxValue]的规则
     *
     * @param required 是否为必填字段
     * @param minValue 最小边界，包含，为null时使用{@link DoubleRule#DEFAULT_MIN_VALUE DoubleRule.DEFAULT_MIN_VALUE}
     * @param maxValue 最大边界，包含，为null时使用{@link DoubleRule#DEFAULT_MAX_VALUE DoubleRule.DEFAULT_MAX_VALUE}
     * @return
     */
    public static RuleChain createDoubleRule(boolean required, Double minValue, Double maxValue) {
        if (required) {
            return new RuleChain("doubleRange", new NotEmptyRule(), new DoubleRule(minValue, maxValue));
        }
        return new RuleChain("optionalDoubleRange", new DoubleRule(minValue, maxValue));
    }

    /**
     * 创建用于验证非空的规则，此处非空指不为null且不是空串
     *
     * @return
     */
    public static RuleChain createNotEmptyRule() {
        return new RuleChain("notEmpty", new NotEmptyRule());
    }

    /**
     * 创建用于验证字符串长度范围的规则
     *
     * @param required  是否为必填字段
     * @param minLength 字符串最小长度，包含，为null则设置为{@link StringRule#DEFAULT_MIN_LENGTH StringRule.DEFAULT_MIN_LENGTH}
     * @param maxLength 字符串最大长度，包含，为null则设置为{@link StringRule#DEFAULT_MAX_LENGTH StringRule.DEFAULT_MAX_LENGTH}
     * @return
     */
    public static RuleChain createStringLengthRule(boolean required, Integer minLength, Integer maxLength) {
        if (required) {
            return new RuleChain("stringLength", new NotNullRule(), new StringRule(minLength, maxLength));
        }
        return new RuleChain("optionalStringLength", new StringRule(minLength, maxLength));
    }

    /**
     * 创建使用正则表达式的验证规则
     *
     * @param required 是否为必填字段
     * @param regExp   用于验证的正则表达式
     * @return
     */
    public static RuleChain createRegExpRule(boolean required, String regExp) {
        if (required) {
            return new RuleChain("regExpRule", new NotNullRule(), new RegExpRule(regExp));
        }
        return new RuleChain("optionalRegExpRule", new RegExpRule(regExp));
    }

    /**
     * 创建验证Email的规则
     *
     * @param required 是否为必填字段
     * @return
     */
    public static RuleChain createEmailRule(boolean required) {
        if (required) {
            return new RuleChain("email", new NotEmptyRule(), new EmailRule());
        }
        return new RuleChain("optionalEmail", new EmailRule());
    }


    /**
     * 从配置文件加载配置规则
     *
     * @param xmlInClassPath 配置文件路径
     * @return
     */
    public static Map<String, RuleChain> loadRuleFromConfig(String xmlInClassPath) {
        RuleChainList ruleList = JAXBUtil.unmarshal(RuleChainList.class, xmlInClassPath);
        List<RuleChain> rules = ruleList.getRules();
        Map<String, RuleChain> map = new HashMap<String, RuleChain>(rules.size());
        for (RuleChain rule : rules) {
            map.put(rule.getName(), rule);
        }
        return ImmutableMap.copyOf(map);
    }
}
