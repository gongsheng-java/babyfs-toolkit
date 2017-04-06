package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * NotEmptyRule测试类
 * <p/>
 */
public class NotEmptyRuleTest {
    @Test
    public void testEmpty() {
    }

    @Test
    public void testNotEmpty() {
        NotEmptyRule rule = new NotEmptyRule();
        assertFalse(rule.validate(null));
        assertFalse(rule.validate(""));
        assertTrue(rule.validate(" "));
    }
}
