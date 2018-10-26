package com.babyfs.tk.apollo.parser;

public interface ParserWrapper {
    Object parse(String rawValue, Class<?> tClass);
}
