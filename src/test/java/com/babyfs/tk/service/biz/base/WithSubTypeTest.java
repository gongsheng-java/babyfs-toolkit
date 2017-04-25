package com.babyfs.tk.service.biz.base;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class WithSubTypeTest {
    @Test
    public void testSubType() {
        ISubType subType = ActivityLogType.ACCOUNT.indexOfSubType(1);
        Assert.assertNotNull(subType);
    }
}