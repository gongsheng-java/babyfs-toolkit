package com.babyfs.tk.apollo.parser;

public class IntegerPlainParser implements PlainParser<Integer> {
    @Override
    public Integer parse(String raw) {
        return Integer.parseInt(raw);
    }
}
