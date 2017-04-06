package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * NotNullRule测试类
 * <p/>
 */
public class NotNullRuleTest {
    @Test
    public void testValidate() {
        NotNullRule rule = new NotNullRule();
        assertFalse(rule.validate(null));
        assertTrue(rule.validate(""));
    }
}
