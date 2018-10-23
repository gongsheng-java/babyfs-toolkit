package com.babyfs.tk.apollo.parser;

import com.alibaba.fastjson.JSON;

public class JsonParserWrapper implements ParserWrapper {
    @Override
    public Object parse(String rawValue, Class tClass) {
        return JSON.parseObject(rawValue, tClass);
    }
}
