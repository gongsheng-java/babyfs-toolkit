package com.babyfs.tk.http.client;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.babyfs.tk.commons.base.Tuple;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 */
public class HttpClientServiceTest {
    @Test
    @Ignore
    public void testConnectWithProxy() throws IOException {
        //测试两种使用验证的方式
        {
            //不用Auth,不会出现警告了: httpclient.HttpMethodDirector: Required proxy credentials not available
            HttpClientProxyConfig proxyConfig = new HttpClientProxyConfig(true, "10.51.35.199", 8080, "", "");
            HttpClientService clientService = new HttpClientService(1, 1000, 2000, proxyConfig);
            String s = clientService.sendGet("http://www.baidu.com", null, null);
            Assert.assertNotNull(s);
        }

        {
            //使用Auth,会出现警告: httpclient.HttpMethodDirector: Required proxy credentials not available
            HttpClientProxyConfig proxyConfig = new HttpClientProxyConfig(true, "10.51.35.199", 8080, " ", "");
            HttpClientService clientService = new HttpClientService(1, 1000, 2000, proxyConfig);
            String s = clientService.sendGet("http://www.baidu.com", null, null);
            Assert.assertNotNull(s);
        }
    }

    @Test
    @Ignore
    public void testUpload() throws IOException {
        HttpClientService clientService = new HttpClientService();
        InputStream inputStream = Resources.getResource("uptest.jpg").openStream();
        Assert.assertNotNull(inputStream);
        Map<String, String> params = Maps.newHashMap();
        params.put("sign_f1", "f2.jpg");
        String response = clientService.sendPost("http://up.org/http_upload", params, null, Tuple.of("f1", "f1.jpg", inputStream));
        System.out.println(response);
    }

    @Test
    @Ignore
    public void test() throws IOException {
        HttpClientService clientService = new HttpClientService();
        String s = clientService.sendGet("http://www.baidu.com", null, null);
        Assert.assertNotNull(s);
        System.out.println(s);
        s = clientService.sendGet("http://www.sohu.com/", null, null, "GBK");
        Assert.assertNotNull(s);
        System.out.println(s);
    }
}
