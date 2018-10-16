package com.babyfs.tk.apollo;


import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * 读取xml时用于替换占位符
 */
public class ApolloStreamReaderDelegate extends StreamReaderDelegate {

    private String namespace;

    public ApolloStreamReaderDelegate(XMLStreamReader reader, String namespace){
        super(reader);
        this.namespace = namespace;
    }

    @Override
    public String getText() {
        String text = super.getText();
        return ConfigLoader.replacePlaceHolder(text);
    }

    @Override
    public String getAttributeValue(int index) {
        String result = super.getAttributeValue(index);
        return ConfigLoader.replacePlaceHolder(namespace, result);
    }

    @Override
    public String getAttributeValue(String namespaceUri, String localName) {
        String result = super.getAttributeValue(namespaceUri, localName);
        return ConfigLoader.replacePlaceHolder(namespace, result);
    }
}
