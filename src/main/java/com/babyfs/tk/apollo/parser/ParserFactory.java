package com.babyfs.tk.apollo.parser;

public class ParserFactory {

    public static <T> ParserWrapper getParser(Class<T> tClass){
        if(PlainParserWrapper.support(tClass)){
            return new PlainParserWrapper();
        }
        return new JsonParserWrapper();
    }
}
