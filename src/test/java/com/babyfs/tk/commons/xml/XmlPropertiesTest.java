package com.babyfs.tk.commons.xml;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 */
public class XmlPropertiesTest {
    @Test
    public void testStoreToXML() throws Exception {
        Map<String, String> maps = Maps.newHashMap();
        maps.put("name1", "王东永");
        maps.put("name2", "donyong.wang@email.com");
        maps.put("desc", "<hello>");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlProperties.storeToXML(maps, out, "xml 配置文件测试");
        out.close();
        String xml = new String(out.toByteArray(), "UTF-8");
        System.out.println(xml);
    }

    @Test
    public void testLoadFromXml() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">" +
                "<properties>\n" +
                "<comment/>\n" +
                "<entry key=\"name2\">donyong.wang@email.com</entry>\n" +
                "<entry key=\"name1\">王东永</entry>\n" +
                "<entry key=\"desc\">&lt;hello&gt;</entry>\n" +
                "</properties>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Map<String, String> map = XmlProperties.loadFromXml(in);
        Assert.assertNotNull(map);
        Assert.assertEquals("王东永", map.get("name1"));
        Assert.assertEquals("<hello>", map.get("desc"));
    }

    @Test
    public void testLoadFromXmlPath() {
        Map<String, String> map = XmlProperties.loadFromXml("properties.xml");
        Assert.assertNotNull(map);
        Assert.assertEquals("王东永", map.get("name1"));
        Assert.assertEquals("<hello>", map.get("desc"));
    }

    @Test
    public void testLoadFromURL() throws IOException {
        URL url = Resources.getResource("properties.xml");
        Map<String, String> map = XmlProperties.loadFromXml(url);
        Assert.assertNotNull(map);
        Assert.assertEquals("王东永", map.get("name1"));
        Assert.assertEquals("<hello>", map.get("desc"));
    }
}
