package com.babyfs.tk.apollo;


import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * 读取xml时用于替换占位符
 */
public class ApolloStreamReaderDelegate extends StreamReaderDelegate {

    public ApolloStreamReaderDelegate(XMLStreamReader reader){
        super(reader);
    }

    @Override
    public String getText() {
        String text = super.getText();
        return ConfigLoader.replacePlaceHolder(text);
    }

    @Override
    public String getAttributeValue(int index) {
        String result = super.getAttributeValue(index);
        return ConfigLoader.replacePlaceHolder(result);
    }

    @Override
    public String getAttributeValue(String namespaceUri, String localName) {
        String result = super.getAttributeValue(namespaceUri, localName);
        return ConfigLoader.replacePlaceHolder(result);
    }
}
