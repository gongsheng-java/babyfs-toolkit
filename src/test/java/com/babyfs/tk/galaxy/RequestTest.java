package com.babyfs.tk.galaxy;


import com.alibaba.fastjson.JSON;
import com.babyfs.tk.commons.codec.impl.HessianCodec;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

@Ignore
public class RequestTest {

    @Test
    public void testPrint() throws Exception {
        //big:{"interfaceName":"com.babyfs.common.service.ITestService","methodSign":"getBig#d070018149011d9b34f9f2da5404b79f","parameters":[83]}
        //small:{"interfaceName":"com.babyfs.common.service.ITestService","methodSign":"getSmall#d070018149011d9b34f9f2da5404b79f","parameters":[83]}
        //middle:{"interfaceName":"com.babyfs.common.service.ITestService","methodSign":"getMiddle#d070018149011d9b34f9f2da5404b79f","parameters":[83]}
        RpcRequest request = JSON.parseObject("{\"interfaceName\":\"com.babyfs.common.service.ITestService\",\"methodSign\":\"getSmall#d070018149011d9b34f9f2da5404b79f\",\"parameters\":[83]}", RpcRequest.class);

        File file = new File("/Users/pi/Downloads/test_small.dat");

        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);
        HessianCodec codec = new HessianCodec();
        fos.write(codec.encode(request));
        fos.close();

    }
}
