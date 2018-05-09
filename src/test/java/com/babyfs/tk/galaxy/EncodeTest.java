package com.babyfs.tk.galaxy;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;


public class EncodeTest {


    @Test
    public void testEncode(){
        HashMap<String,Object> map = Maps.newHashMap();
        List<TestList> list = Lists.newArrayList();
        TestList testClass = new TestList();
        testClass.setName("sssss");
        testClass.setDate(new Date(System.currentTimeMillis()));
        list.add(testClass);
        map.put("sss",testClass);

    }

    @Test
    public void testSqlDate(){
        Date date = new Date(System.currentTimeMillis());

    }


}
