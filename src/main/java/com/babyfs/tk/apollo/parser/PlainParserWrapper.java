package com.babyfs.tk.apollo.parser;

import java.util.HashMap;
import java.util.Map;

public class PlainParserWrapper implements ParserWrapper {

    private static final Map<Class, PlainParser> container = new HashMap<>();

    static {
        register(Integer.class, new IntegerPlainParser());
        register(Float.class, new FloatPlainParser());
        register(String.class, new StringPlainParser());
    }

    private static <T> void register(Class<T> tClass, PlainParser<T> plainParser){
        container.put(tClass, plainParser);
    }

    @Override
    public Object parse(String rawValue, Class<?> tClass) {
        PlainParser parser = container.get(tClass);
        if(parser == null) throw new RuntimeException("unregistered parser");
        return parser.parse(rawValue);
    }

    public static boolean support(Class tClass){
        return container.containsKey(tClass);
    }
}
