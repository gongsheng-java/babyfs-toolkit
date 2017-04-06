package com.babyfs.tk.commons.config.internal;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class ConfigServiceURIImplTest {

    @Test
    public void test_load() throws MalformedURLException, URISyntaxException {
        //load from classpath
        loadFromClasspath("classpath:///properties.xml");
        loadFromClasspath("properties.xml");
        loadFromClasspath(Resources.getResource("properties.xml").toURI().toString());
    }

    private void loadFromClasspath(String path) throws URISyntaxException {
        ConfigServiceURIImpl configServiceCommonURI = new ConfigServiceURIImpl(Lists.newArrayList(new URI(path)));
        Assert.assertNotNull(configServiceCommonURI);
        String name2 = configServiceCommonURI.get("name2");
        Assert.assertEquals("donyong.wang@email.com", name2);
        Assert.assertEquals("王东永", configServiceCommonURI.get("name1"));
        Set<String> keys = configServiceCommonURI.keySet();
        Assert.assertNotNull(keys);
        System.out.println(keys);
    }

}