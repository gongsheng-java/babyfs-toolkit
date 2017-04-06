package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * StringRule的测试类
 * <p/>
 */
public class StringRuleTest {
    @Test
    public void testDefaultLengthRange() {
        StringRule rule = new StringRule(null, null);
        assertTrue(rule.getMinLength() == StringRule.DEFAULT_MIN_LENGTH);
        assertTrue(rule.getMaxLength() == StringRule.DEFAULT_MAX_LENGTH);
    }

    @Test
    public void testLengthRange() {
        StringRule rule = new StringRule(1, 10);
        assertTrue(rule.validate(null));
        assertFalse(rule.validate(""));
        assertTrue(rule.validate("a"));
        assertTrue(rule.validate("abcde"));
        assertTrue(rule.validate("abcdefghij"));
        assertFalse(rule.validate("abcdefghijk"));
    }

    @Test
    public void testEmptyRange() {
        StringRule rule = new StringRule(0, 0);
        assertTrue(rule.validate(null));
        assertTrue(rule.validate(""));
        assertFalse(rule.validate("a"));
    }
}
