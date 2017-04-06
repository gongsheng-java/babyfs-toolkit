package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * EmailRule测试类
 * <p/>
 */
public class EmailRuleTest {
    @Test
    public void testValidate() {
        EmailRule rule = new EmailRule();
        assertTrue(rule.validate(null));
        assertTrue(rule.validate(""));
        assertTrue(rule.validate("a.b@c.d"));
    }
}
