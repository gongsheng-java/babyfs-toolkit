package com.babyfs.tk.service.biz.validator.guice;

import com.babyfs.tk.service.biz.validator.IValidateService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.commons.validator.RuleChain;
import com.babyfs.tk.commons.validator.ValidatorRuleFactory;
import com.babyfs.tk.service.biz.validator.impl.ValidateServiceImpl;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * 验证服务Module
 * <p/>
 * 使用该服务时，需要通过
 * bindConstant().annotatedWith(Names.named(GlobalKeys.VALIDATION_RULE_CONF)).to(验证规则文件列表)
 * 来注入验证规则文件
 * <p/>
 */
public class ValidateServiceModule extends ServiceModule {
    /**
     * 默认的验证规则配置文件
     */
    public static final String DEFAULT_VALIDATE_RULE_CONF = "validation_rule.xml";
    /**
     * 是否启动严格模式
     */
    private final boolean strictMode;

    /**
     * 验证规则的配置文件
     */
    private final String ruleConfs;

    public ValidateServiceModule() {
        this(true);
    }

    public ValidateServiceModule(boolean strictMode) {
        this(strictMode, DEFAULT_VALIDATE_RULE_CONF);
    }

    public ValidateServiceModule(boolean strictMode, String ruleConfs) {
        this.strictMode = strictMode;
        this.ruleConfs = Preconditions.checkNotNull(StringUtils.trimToNull(ruleConfs), "validation rule configuration file does not set");
    }

    @Override
    protected void configure() {
        Map<String, RuleChain> stringRuleChainMap = get();
        Preconditions.checkState(!stringRuleChainMap.isEmpty(), "Can't find rule from confis %s", this.ruleConfs);
        ValidateServiceImpl instance = new ValidateServiceImpl(stringRuleChainMap, strictMode);
        bindService(IValidateService.class, instance);
    }


    protected Map<String, RuleChain> get() {
        String[] confs = this.ruleConfs.split(",");
        Map<String, RuleChain> temp = new HashMap<String, RuleChain>();
        for (String conf : confs) {
            if (Strings.isNullOrEmpty(conf)) {
                continue;
            }
            temp.putAll(ValidatorRuleFactory.loadRuleFromConfig(conf));
        }
        return ImmutableMap.copyOf(temp);
    }
}
