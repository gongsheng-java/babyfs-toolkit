package com.babyfs.tk.commons.validator.rule;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class IdCardRuleTest {
    @Test
    public void validate() throws Exception {
        IdCardRule idCardRule = new IdCardRule();
        Assert.assertTrue(idCardRule.validate("34052419800101001X"));
        Assert.assertFalse(idCardRule.validate("340524198001010010"));
        Assert.assertFalse(idCardRule.validate("340524198001010011"));
    }
}