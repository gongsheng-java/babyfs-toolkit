package com.babyfs.tk.commons.validator.rule;

import org.junit.Test;

import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * RegExpRule的测试类
 * <p/>
 */
public class RegExpRuleTest {
    @Test
    public void testErrorRegExp() {
        try {
            assertTrue(new RegExpRule("??") == null);
        } catch (Exception e) {
            assertTrue(e instanceof PatternSyntaxException);
        }
    }

    @Test
    public void testRegExp() {
        RegExpRule rule = new RegExpRule("\\d+");
        assertFalse(rule.validate("1a1"));
        assertTrue(rule.validate(null));
        assertFalse(rule.validate(""));
        assertTrue(rule.validate("11"));
        assertFalse(rule.validate("-1"));
        assertTrue(rule.validate("0"));
        assertTrue(rule.validate("01723984619"));
    }
}
