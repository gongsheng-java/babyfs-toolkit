package com.babyfs.tk.apollo.parser;

public interface PlainParser<T> {
    T parse(String raw);
}
