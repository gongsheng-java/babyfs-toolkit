package com.babyfs.tk.commons.validator;

import com.google.common.base.Strings;
import com.babyfs.tk.commons.validator.rule.EmailRule;
import com.babyfs.tk.commons.validator.rule.NotEmptyRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * <p/>
 */
public class RuleChainTest {

    @Test
    public void testRuleChain() {
        NotEmptyRule notEmptyRule = new NotEmptyRule();
        EmailRule emailRule = new EmailRule();
        RuleChain rule = new RuleChain("email",notEmptyRule.getErrorMsg(), notEmptyRule, emailRule);

        {
            ValidateResult result = rule.validate(null);
            assertFalse(result.isSuccess());
            assertEquals(notEmptyRule.getErrorMsg(), result.getErrorMsg());
        }

        {
            ValidateResult result = rule.validate("jiahao.fang@renren-inc.com");
            assertTrue(result.isSuccess());
            assertTrue(Strings.isNullOrEmpty(result.getErrorMsg()));
        }

        {
            ValidateResult result = rule.validate("@renren-inc.com");
            assertFalse(result.isSuccess());
        }
    }
}
