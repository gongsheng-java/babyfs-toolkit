package com.babyfs.tk.galaxy;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.codec.util.ProtostuffCodecUtil;
import org.junit.Test;


public class EncodeTest {


    @Test
    public void testEncode(){
        TestClass testClass = new TestClass("aaa");
        byte[] data = ProtostuffCodecUtil.encode(testClass);
        TestClass test = ProtostuffCodecUtil.decode(data, TestClass.class);
        System.out.println(test.name);
    }

    private class TestClass  {
        private String name;

        public TestClass(String name) {
            this.name = name;
        }
    }


}
