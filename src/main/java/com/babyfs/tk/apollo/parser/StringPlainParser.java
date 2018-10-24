package com.babyfs.tk.apollo.parser;

public class StringPlainParser implements PlainParser<String> {
    @Override
    public String parse(String raw) {
        return raw;
    }
}
