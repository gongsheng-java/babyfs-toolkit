package com.babyfs.tk.commons.xml;

import com.babyfs.tk.apollo.ConfigLoader;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 读取写入XML格式的{@link Properties}
 */
public final class XmlProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlProperties.class);

    private XmlProperties() {
    }

    /**
     * 加载指定名称的XML配置文件,按照如下的顺序查找配置文件:
     * 1. class path
     * 2. file path
     *
     * @param xmlPropertiesPath
     * @return 如果未找到对应的配置文件, 则返回null
     */
    public static Map<String, String> loadFromXml(String xmlPropertiesPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xmlPropertiesPath), "xmlPropertiesPath");
        InputStream in = null;
        try {
            in = Resources.asByteSource(Resources.getResource(xmlPropertiesPath)).openStream();
            if (in != null) {
                LOGGER.info("Found the xml properties [{}] in class path,use it", xmlPropertiesPath);
                return loadFromXml(in);
            }
            File inFile = new File(xmlPropertiesPath);
            if (inFile.isFile()) {
                LOGGER.info("Found the xml properties [{}] in file path,use it", xmlPropertiesPath);
                in = new FileInputStream(new File(xmlPropertiesPath));
                return loadFromXml(in);
            }
        } catch (Exception e) {
            LOGGER.error("Load xml properties [" + xmlPropertiesPath + "] error.", e);
        } finally {
            try {
                Closeables.close(in, true);
            } catch (IOException e) {
                // Ignore
            }
        }
        LOGGER.warn("Can't find the xml properties file [{}] in both class and file path", xmlPropertiesPath);
        return null;
    }

    /**
     * 从输入流中加载配置文件
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadFromXml(InputStream in) throws IOException {
        Map<String, String> map = Maps.newHashMap();
        try {
            Preconditions.checkNotNull(in, "in");
            Properties properties = new Properties();
            properties.loadFromXML(in);
            Set<Map.Entry<Object, Object>> entries = properties.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {//增加占位符替换
                map.put((String) entry.getKey(), ConfigLoader.replacePlaceHolder((String) entry.getValue()));
            }
        } finally {
            Closeables.close(in, true);
        }
        return map;
    }

    /**
     * 从URL中加载配置文件
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadFromXml(URL url) throws IOException {
        Preconditions.checkNotNull(url, "url");
        return loadFromXml(url.openStream());
    }

    /**
     * 将配置以xml格式写入到输出流中
     *
     * @param map
     * @param out
     * @throws IOException
     */
    public static void storeToXML(Map<String, String> map, OutputStream out, String comment) throws IOException {
        try {
            Preconditions.checkNotNull(map, "map");
            Preconditions.checkNotNull(out, "out");
            Properties properties = new Properties();
            properties.putAll(map);
            properties.storeToXML(out, comment);
        } finally {
            Closeables.close(out, true);
        }
    }
}
