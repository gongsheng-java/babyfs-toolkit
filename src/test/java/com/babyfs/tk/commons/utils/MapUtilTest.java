package com.babyfs.tk.commons.utils;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MapUtilTest {

    @Test
    public void testGet() throws Exception {
        Map<String, String> map = Maps.newHashMap();
        String s = MapUtil.get(map, "name", "wdy");
        Assert.assertEquals("wdy", s);
        map.put("name", "w");
        s = MapUtil.get(map, "name", "wdy");
        Assert.assertEquals("w", s);

        Map map2 = Maps.newHashMap();
        map2.put("age1", 16);
        Object age1 = MapUtil.get(map2, "age1", 1);
        Assert.assertEquals(16, age1);
        Object age2 = MapUtil.get(map2, "age2", null);
        Assert.assertNull(age2);
        age2 = MapUtil.get(map2, "age2", 1.0);
        Assert.assertEquals(1.0, age2);
    }
}