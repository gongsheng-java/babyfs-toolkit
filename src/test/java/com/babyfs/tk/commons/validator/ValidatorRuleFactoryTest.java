package com.babyfs.tk.commons.validator;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * ValidatorRuleFactory测试类
 * <p/>
 */
public class ValidatorRuleFactoryTest {
    @Test
    public void testCreateNonNegativeIntRule() {
        RuleChain rule = ValidatorRuleFactory.createNonNegativeIntRule(false);
        assertTrue(rule.validate(null).isSuccess());
        assertTrue(rule.validate("").isSuccess());
        assertTrue(rule.validate("0").isSuccess());
        assertTrue(rule.validate("1").isSuccess());
        assertFalse(rule.validate("-1").isSuccess());
    }

    @Test
    public void testCreateIntRangeRule() {
        RuleChain rule = ValidatorRuleFactory.createIntRangeRule(true, -1, 10);
        assertFalse(rule.validate(null).isSuccess());
        assertFalse(rule.validate("").isSuccess());
        assertFalse(rule.validate("-2").isSuccess());
        assertTrue(rule.validate("-1").isSuccess());
        assertTrue(rule.validate("0").isSuccess());
        assertTrue(rule.validate("1").isSuccess());
        assertTrue(rule.validate("5").isSuccess());
        assertTrue(rule.validate("10").isSuccess());
        assertFalse(rule.validate("11").isSuccess());
    }

    @Test
    public void testCreateNotEmptyRule() {
        RuleChain rule = ValidatorRuleFactory.createNotEmptyRule();
        assertFalse(rule.validate(null).isSuccess());
        assertFalse(rule.validate("").isSuccess());
        assertTrue(rule.validate(" ").isSuccess());
    }

    @Test
    public void testCreateStringLengthRule() {
        RuleChain rule = ValidatorRuleFactory.createStringLengthRule(true, 1, 10);
        assertFalse(rule.validate(null).isSuccess());
        assertFalse(rule.validate("").isSuccess());
        assertTrue(rule.validate("1").isSuccess());
        assertTrue(rule.validate("abcde12345").isSuccess());
        assertFalse(rule.validate("abcde123456").isSuccess());
    }

    @Test
    public void testCreateRegExpRule() {
        RuleChain rule = ValidatorRuleFactory.createRegExpRule(false, "\\d+");
        assertTrue(rule.validate(null).isSuccess());
        assertFalse(rule.validate("").isSuccess());
        assertFalse(rule.validate("1a").isSuccess());
        assertTrue(rule.validate("11").isSuccess());
        assertTrue(rule.validate("1").isSuccess());
    }

    @Test
    public void testCreateEmailRule() {
        RuleChain rule = ValidatorRuleFactory.createEmailRule(true);
        assertFalse(rule.validate(null).isSuccess());
        assertFalse(rule.validate("").isSuccess());
        assertFalse(rule.validate("1a").isSuccess());
        assertTrue(rule.validate("a@b.c").isSuccess());
        assertFalse(rule.validate("a@b@c.e").isSuccess());
    }

    @Test
    public void testLoadRuleFromConfig() {
        Map<String, RuleChain> map = ValidatorRuleFactory.loadRuleFromConfig("test.xml");
        assertEquals(7, map.size());
        {
            RuleChain rule = map.get("optionalIntRange");
            assertNotNull(rule);
            assertTrue(rule.validate(null).isSuccess());
            assertTrue(rule.validate("").isSuccess());
            assertFalse(rule.validate("-11").isSuccess());
            assertTrue(rule.validate("-10").isSuccess());
            assertTrue(rule.validate("10").isSuccess());
            assertFalse(rule.validate("11").isSuccess());
            assertFalse(rule.validate("1a1").isSuccess());
        }
        {
            RuleChain rule = map.get("requiredEmail");
            assertNotNull(rule);
            assertFalse(rule.validate(null).isSuccess());
            assertFalse(rule.validate("").isSuccess());
            assertFalse(rule.validate("1a").isSuccess());
            assertTrue(rule.validate("a@b.c").isSuccess());
            assertFalse(rule.validate("a@b@c.e").isSuccess());
        }
        {
            RuleChain rule = map.get("long");
            assertNotNull(rule);
            assertFalse(rule.validate(null).isSuccess());
            assertFalse(rule.validate("").isSuccess());
            assertFalse(rule.validate("1a").isSuccess());
            assertTrue(rule.validate("1").isSuccess());
            assertFalse(rule.validate("10000000000000000000").isSuccess());
        }
    }

}
