package com.babyfs.tk.service.biz.service.validator.internal;

import com.babyfs.tk.commons.validator.RuleChain;
import com.babyfs.tk.commons.validator.ValidatorRuleFactory;
import com.babyfs.tk.service.biz.service.validator.IValidateService;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ValidateServiceImpl ValidateServiceImpl}测试类
 * <p/>
 */
public class ValidateServiceImplTest {

    private Map<String, RuleChain> ruleMap;

    @Before
    public void setUp() throws Exception {
        this.ruleMap = new HashMap<String, RuleChain>();
        ruleMap.put("rule1", ValidatorRuleFactory.createStringLengthRule(true, 1, 10));
        ruleMap.put("rule2", ValidatorRuleFactory.createIntRangeRule(true, null, 10));
        ruleMap.put("rule3", ValidatorRuleFactory.createRegExpRule(true, "[a-z]{1,5}"));
    }

    @Test
    public void testValidate() {
        IValidateService service = new ValidateServiceImpl(this.ruleMap, true);
        {
            assertTrue(service.validate("rule1", "abcd").isSuccess());
            assertFalse(service.validate("rule1", null).isSuccess());
            assertTrue(service.validate("rule1", "a").isSuccess());
            assertTrue(service.validate("rule1", "0123456789").isSuccess());
            assertFalse(service.validate("rule1", "").isSuccess());
            assertFalse(service.validate("rule1", "01234567890").isSuccess());
        }
        {
            assertFalse(service.validate("rule2", null).isSuccess());
            assertFalse(service.validate("rule2", "").isSuccess());
            assertFalse(service.validate("rule2", "1a1").isSuccess());
            assertTrue(service.validate("rule2", "-1").isSuccess());
            assertTrue(service.validate("rule2", "10").isSuccess());
            assertTrue(service.validate("rule2", String.valueOf(Integer.MIN_VALUE)).isSuccess());
            assertFalse(service.validate("rule2", "11").isSuccess());
        }
        {
            assertFalse(service.validate("rule3", null).isSuccess());
            assertTrue(service.validate("rule3", "a").isSuccess());
            assertTrue(service.validate("rule3", "za").isSuccess());
            assertFalse(service.validate("rule3", "a1").isSuccess());
            assertTrue(service.validate("rule3", "abcde").isSuccess());
            assertTrue(service.validate("rule3", "aeiou").isSuccess());
            assertFalse(service.validate("rule3", "abcdef").isSuccess());
        }
    }
}
