package com.babyfs.tk.http.client;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.http.constants.HttpClientConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

/**
 * Http客户端服务测试类
 * <p/>
 */
public class HttpClientTest implements Serializable {

    private static final String URL = "http://www.baidu.com";

    @Test
    @Ignore
    public void testMain() {
        HttpClientService clientService = new HttpClientService(20, 1000, 1000);
        try {
            String response = clientService.sendGet(URL, null, null);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        clientService.close();
    }


    @Test
    @Ignore
    public void testImage() {
        String url = "http://a1987.phobos.apple.com/us/r1000/002/Purple/b3/72/f4/mzl.ovzitmtg.320x480-75.jpg";
        HttpClientService clientService = new HttpClientService(20, 3000, 3000);
        try {
            Pair<String, byte[]> bs = clientService.sendGetForRaw(url, null, null);
            InputStream inputStream = new ByteArrayInputStream(bs.second);
            FileOutputStream outputStream = new FileOutputStream(new File("target", "aaaaa.jpg"));
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        clientService.close();
    }

    /**
     * 常规测试 assemblyGetParams
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void testAssemblyGet_normal() throws Exception {
        HttpClientService clientService = new HttpClientService();
        Map<String, String> getParams = new TreeMap<String, String>();
        getParams.put("key1", "value1");
        getParams.put("key2", "value2");
        getParams.put("key3", "value3");
        URI uri = HttpClientUtils.assemblyGetURI(URL, getParams);
        String response = clientService.sendGet(URL, getParams, null);
        Assert.assertEquals("key1=value1&key2=value2&key3=value3", uri.getQuery());
    }

    /**
     * URL编码测试 assemblyGetParams
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void testAssemblyGet_charset() throws Exception {
        Map<String, String> getParams = new TreeMap<String, String>();
        getParams.put("键1", "值1");
        getParams.put("键2", "值2");
        getParams.put("键3", "值3");
        URI uri = HttpClientUtils.assemblyGetURI(URL, getParams);
        String expected = "%E9%94%AE1=%E5%80%BC1&%E9%94%AE2=%E5%80%BC2&%E9%94%AE3=%E5%80%BC3";
        Assert.assertEquals(expected, uri.getRawQuery());
    }

    /**
     * 测试 assemblyPostParams
     */
    @Test
    @Ignore
    public void testAssemblyPost() throws IOException {
        HttpClientService clientService = new HttpClientService();
        HttpPost postMethod = new HttpPost(URL);
        Map<String, String> postParams = new TreeMap<String, String>();
        postParams.put("键1", "值1");
        postParams.put("键2", "值2");
        postParams.put("键3", "值3");
        HttpClientUtils.assemblyPostParams(postMethod, postParams);
        //String responseCode = clientService.sendPost(URL, postParams, null);
        String expected = "%E9%94%AE1=%E5%80%BC1&%E9%94%AE2=%E5%80%BC2&%E9%94%AE3=%E5%80%BC3";
        String actual = EntityUtils.toString(postMethod.getEntity());
        Assert.assertEquals(expected, actual);
        clientService.close();

    }

    /**
     * 测试 uncompressStream
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void testUncompressStream() throws IOException {

        final String FILE_NAME = "compress.txt";
        File file = new File(FILE_NAME);
        file.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        char[] fileBytes = {'a', 'b', 'c'};
        String expectStr = String.valueOf(fileBytes);
        gzipOutputStream.write(fileBytes[0]);
        gzipOutputStream.write(fileBytes[1]);
        gzipOutputStream.write(fileBytes[2]);
        gzipOutputStream.close();
        fileOutputStream.close();


        FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
        byte[] inputBytes = new byte[128];
        fileInputStream.read(inputBytes);
        fileInputStream.close();
        System.out.println(Arrays.toString(inputBytes));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputBytes);
        String actual = HttpClientUtils.uncompressStream(
                HttpClientConfig.CompressFormat.COMPRESS_FORMAT_GZIP, byteArrayInputStream, null);
        byteArrayInputStream.close();
        file = new File(FILE_NAME);
        file.deleteOnExit();

        Assert.assertEquals(expectStr, actual);
    }

    /**
     * 测试proxy
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void testProxy() throws IOException {
        //本地启Ngnix，具体配置参考：http://www.reistlin.com/blog/301，
        /*
        HttpClientProxyConfig config = new HttpClientProxyConfig(true, "127.0.0.1", 8089, "name", "password");
        HttpClientService clientService = new HttpClientService(100, 2000, 2000, config);
        String response = clientService.sendGet(URL, null, null);
        assert !Strings.isNullOrEmpty(response);
         */
    }

}
