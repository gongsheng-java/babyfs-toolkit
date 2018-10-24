package com.babyfs.tk.apollo.parser;

public class FloatPlainParser implements PlainParser<Float> {
    @Override
    public Float parse(String raw) {
        return Float.parseFloat(raw);
    }
}
