package com.babyfs.tk.service.basic.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * JSON和对象之间的转换
 */
public class JSONConverter<A> extends Converter<A, String> {
    public static final SerializeFilter[] EMPTY_SERIALIZE_FILTERS = new SerializeFilter[0];
    public static final SerializerFeature[] EMPTY_SERIALIZER_FEATURES = new SerializerFeature[0];
    public static final Feature[] EMPTY_PARSE_FEATURES = new Feature[0];
    public static final SerializeConfig DEFAULT_SERIALIZE_CONFIG = SerializeConfig.getGlobalInstance();
    public static final ParserConfig DEFAULT_PARSER_CONFIG = ParserConfig.getGlobalInstance();

    private final Class<A> type;
    private final SerializeConfig serializeConfig;
    private final ParserConfig parserConfig;
    private final SerializeFilter[] serializeFilters;
    private final SerializerFeature[] serializeFeatures;
    private final Feature[] parseFeatures;
    private final Function<A, A> parseValueAdaptor;

    /**
     *
     */
    public JSONConverter(Class<A> type) {
        this(type, null);
    }

    /**
     * @param type              非空
     * @param parseValueAdaptor 从JSON String反序列化数据后,可以使用这个converter对数据进行适配,可以为空
     */
    public JSONConverter(Class<A> type, Function<A, A> parseValueAdaptor) {
        this(type, DEFAULT_SERIALIZE_CONFIG, DEFAULT_PARSER_CONFIG, EMPTY_SERIALIZER_FEATURES, EMPTY_SERIALIZE_FILTERS, EMPTY_PARSE_FEATURES, parseValueAdaptor);
    }

    /**
     * @param type              非空
     * @param serializeConfig   非空
     * @param parserConfig      非空
     * @param serializeFeatures 可以为空
     * @param serializeFilters  可以为空
     * @param parseFeatures     可以为空
     * @param parseValueAdaptor 从JSON String反序列化数据后,可以使用这个converter对数据进行适配,可以为空
     */
    public JSONConverter(Class<A> type, SerializeConfig serializeConfig, ParserConfig parserConfig, SerializerFeature[] serializeFeatures, SerializeFilter[] serializeFilters, Feature[] parseFeatures, Function<A, A> parseValueAdaptor) {
        this.parseValueAdaptor = parseValueAdaptor;
        this.type = Preconditions.checkNotNull(type);
        this.serializeConfig = Preconditions.checkNotNull(serializeConfig);
        this.parserConfig = Preconditions.checkNotNull(parserConfig);
        this.serializeFeatures = Preconditions.checkNotNull(serializeFeatures);
        this.serializeFilters = Preconditions.checkNotNull(serializeFilters);
        this.parseFeatures = Preconditions.checkNotNull(parseFeatures);
    }

    @Override
    protected String doForward(A a) {
        return JSON.toJSONString(a, serializeConfig, serializeFilters, this.serializeFeatures);
    }

    @Override
    protected A doBackward(String s) {
        A a = JSON.parseObject(s, type, parserConfig, JSON.DEFAULT_PARSER_FEATURE, this.parseFeatures);
        if (parseValueAdaptor != null) {
            return parseValueAdaptor.apply(a);
        } else {
            return a;
        }
    }
}
