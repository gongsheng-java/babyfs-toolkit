package com.babyfs.tk.service.biz.op.user;

import com.babyfs.tk.service.biz.op.user.Util;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class UtilTest {
    @Test
    public void getOriginUserName() throws Exception {
        String name = "w";
        Assert.assertEquals("w", Util.getOriginUserName(name));
        Assert.assertEquals("w", Util.getOriginUserName("w@"));
        Assert.assertEquals("w", Util.getOriginUserName("w@aa.com"));
        Assert.assertEquals("w@aa.com", Util.getOriginUserName("w@aa.com@ldap"));
        Assert.assertEquals("", Util.getOriginUserName("@"));
    }
}