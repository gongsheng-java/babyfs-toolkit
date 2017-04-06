package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * IntegerRule的测试类
 * <p/>
 */
public class IntegerRuleTest {
    @Test
    public void testInvalidInt() {
        IntegerRule rule = new IntegerRule(null, null);
        assertTrue(rule.validate(""));
        assertTrue(rule.validate(null));
        assertFalse(rule.validate("1a"));
    }

    @Test
    public void testDefaultRange() {
        IntegerRule rule = new IntegerRule(null, null);
        assertTrue(rule.getMinValue() == IntegerRule.DEFAULT_MIN_VALUE);
        assertTrue(rule.getMaxValue() == IntegerRule.DEFAULT_MAX_VALUE);
        assertTrue(rule.validate(String.valueOf(IntegerRule.DEFAULT_MIN_VALUE)));
        assertTrue(rule.validate(String.valueOf(IntegerRule.DEFAULT_MAX_VALUE)));
    }

    @Test
    public void testRange() {
        IntegerRule rule = new IntegerRule(-1, 10);
        assertTrue(rule.validate("-1"));
        assertTrue(rule.validate("0"));
        assertTrue(rule.validate("1"));
        assertTrue(rule.validate("10"));
        assertFalse(rule.validate("-2"));
        assertFalse(rule.validate("11"));
    }

    @Test
    public void testDefaultMinRange() {
        IntegerRule rule = new IntegerRule(null, 10);
        assertTrue(rule.getMinValue() == IntegerRule.DEFAULT_MIN_VALUE);
        assertTrue(rule.validate(String.valueOf(IntegerRule.DEFAULT_MIN_VALUE)));
        assertTrue(rule.validate("-1"));
        assertTrue(rule.validate("0"));
        assertTrue(rule.validate("1"));
        assertTrue(rule.validate("10"));
        assertFalse(rule.validate("11"));
    }

    @Test
    public void testDefaultMaxRange() {
        IntegerRule rule = new IntegerRule(-1, null);
        assertTrue(rule.getMaxValue() == IntegerRule.DEFAULT_MAX_VALUE);
        assertTrue(rule.validate(String.valueOf(IntegerRule.DEFAULT_MAX_VALUE)));
        assertTrue(rule.validate("-1"));
        assertFalse(rule.validate("-2"));
        assertTrue(rule.validate("0"));
        assertTrue(rule.validate("1"));
        assertTrue(rule.validate("10"));
    }
}
