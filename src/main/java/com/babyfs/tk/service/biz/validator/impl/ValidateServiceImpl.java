package com.babyfs.tk.service.biz.validator.impl;

import com.babyfs.tk.service.biz.validator.IValidateService;
import com.google.common.base.Preconditions;
import com.google.inject.name.Named;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.commons.validator.RuleChain;
import com.babyfs.tk.commons.validator.ValidateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 数据验证类
 * <p/>
 */
public class ValidateServiceImpl implements IValidateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateServiceImpl.class);
    /**
     * 验证规则Map
     */
    private final Map<String, RuleChain> ruleMap;
    /**
     * 是否是严格模式，默认为true，在严格模式下，未找到匹配规则的字段一致认为验证失败，非严格模式下，一致认为验证成功
     */
    private final boolean strictMode;


    @Inject
    public ValidateServiceImpl(Map<String, RuleChain> ruleMap,
                               @Named(GlobalKeys.VALIDATION_STRICT_MODE) boolean strictMode) {
        Preconditions.checkArgument(ruleMap != null, "ruleMap can not be null");
        this.ruleMap = ruleMap;
        this.strictMode = strictMode;
    }

    @Override
    public ValidateResult validate(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            ValidateResult res = validate(entry.getKey(), entry.getValue());
            if (!res.isSuccess())
                return res;
        }
        return ValidateResult.RESULT_OK;
    }

    @Override
    public ValidateResult validate(String key, String value) {
        RuleChain rule = ruleMap.get(key);
        if (rule == null) {
            if (strictMode) {
                LOGGER.warn("validate rule with name {} not found", key);
                return new ValidateResult(false, "Internal Error");
            } else {
                return ValidateResult.RESULT_OK;
            }
        }
        return rule.validate(value);
    }

    @Override
    public ValidateResult validate(HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            String value = (values != null && values.length != 0) ? values[0] : null;
            ValidateResult res = validate(key, value);
            if (!res.isSuccess())
                return res;
        }
        return ValidateResult.RESULT_OK;
    }

    @Override
    public ValidateResult validateParamByRule(String... ruleAndParams) {
        Preconditions.checkNotNull(ruleAndParams, "Can't valiad null params");
        Preconditions.checkArgument(ruleAndParams.length % 2 == 0, "The length of params must be even ");
        for (int i = 0; i < ruleAndParams.length; i += 2) {
            String ruleName = ruleAndParams[i];
            String value = ruleAndParams[i + 1];
            ValidateResult result = this.validate(ruleName, value);
            if (result == null || !result.isSuccess()) {
                return result;
            }
        }
        return ValidateResult.RESULT_OK;
    }

    @Override
    public ValidateResult validateParamByRule(List<String> ruleAndParams) {
        Preconditions.checkNotNull(ruleAndParams, "Can't valiad null params");
        Preconditions.checkArgument(ruleAndParams.size() % 2 == 0, "The length of params must be even ");
        for (int i = 0; i < ruleAndParams.size(); i += 2) {
            String ruleName = ruleAndParams.get(i);
            String value = ruleAndParams.get(i + 1);
            ValidateResult result = this.validate(ruleName, value);
            if (result == null || !result.isSuccess()) {
                return result;
            }
        }
        return ValidateResult.RESULT_OK;
    }
}
