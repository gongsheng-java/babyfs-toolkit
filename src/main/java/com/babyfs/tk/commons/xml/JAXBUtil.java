package com.babyfs.tk.commons.xml;

import com.babyfs.tk.apollo.ApolloStreamReaderDelegate;
import com.babyfs.tk.apollo.ApolloUtil;
import com.babyfs.tk.apollo.ConfigLoader;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;

import javax.xml.bind.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;

/**
 * XML解析的工具类
 */
public final class JAXBUtil {

    /**
     * 反序列化的事件处理器
     */
    private static final ValidationEventHandler UNMARSHALLER_HANDLER = new ValidationEventHandler() {
        @Override
        public boolean handleEvent(ValidationEvent event) {
            return event.getSeverity() == ValidationEvent.WARNING;
        }
    };

    private JAXBUtil() {

    }

    /**
     * 从指定的xml文件中解析出<code>T</code>类型的对象
     *
     * @param clazz
     * @param xmlInClassPath
     * @param <T>
     * @return
     * @throws RuntimeException
     */
    public static <T> T unmarshal(Class<T> clazz, String xmlInClassPath) {
        Reader reader = null;
        try {
            URL xmlUrl = Resources.getResource(xmlInClassPath);
            reader = new InputStreamReader(xmlUrl.openStream(), "UTF-8");
            //增加占位符替换逻辑
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);
            StreamReaderDelegate streamReaderDelegate = new ApolloStreamReaderDelegate(xmlStreamReader, ApolloUtil.getNamespace(xmlInClassPath, "xml"));//替换占位符
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(UNMARSHALLER_HANDLER);
            return (T) unmarshaller.unmarshal(streamReaderDelegate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unmarshal object for class:" + clazz + " xml:" + xmlInClassPath, e);
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    /**
     * 从指定的xml字符串中解析出<code>T</code>类型的对象
     *
     * @param clazz
     * @param xmlContent
     * @param <T>
     * @return
     * @throws RuntimeException
     */
    public static <T> T unmarshalByContent(Class<T> clazz, String xmlContent) {
        Reader reader = null;
        try {
            reader = new StringReader(xmlContent);
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(UNMARSHALLER_HANDLER);
            return (T) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unmarshal object for class:" + clazz + " xmlContent:" + xmlContent, e);
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    public static void marshal(Object obj, OutputStream out) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(obj, out);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    public static void marshal(Object obj, Writer writer) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(obj, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }
}
