package com.babyfs.tk.rpc.codec;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.codec.impl.HessianCodec;
import com.babyfs.tk.commons.codec.impl.JavaCodec;
import com.babyfs.tk.commons.codec.impl.ProtobuffCodec;

import java.util.concurrent.ConcurrentMap;

/**
 * 内置支持的编码解码器大全
 */
public final class Codecs {
    private static final ConcurrentMap<Byte, ICodec> CODECS = new MapMaker().makeMap();

    /**
     * Java内置的序列化编码
     */
    public static final ICodec JAVA_CODEC = new JavaCodec();
    /**
     * 由Ressin提供的序列化编码机制
     */
    public static final ICodec HESSIAN_CODEC = new HessianCodec();
    /**
     * 由Google Protobuffer提供的序列化编码机制
     */
    public static final ICodec PROTOBUFF_CODEC = new ProtobuffCodec();

    private Codecs() {

    }

    //注册内置的编码解码器
    static {
        add(JAVA_CODEC);
        add(HESSIAN_CODEC);
        add(PROTOBUFF_CODEC);
    }

    /**
     * 取得指定类型的编解码器
     *
     * @param type
     * @return
     */
    public static ICodec getCodecByType(byte type) {
        return CODECS.get(type);
    }

    /**
     * 注册一个编解码器
     *
     * @param codec
     * @throws IllegalStateException 如果{@link ICodec#getType()} 已经被注册,则会抛出此异常
     */
    public static void add(ICodec codec) {
        Preconditions.checkNotNull(codec, "The codec must be set.");
        ICodec preCodec = CODECS.putIfAbsent(codec.getType(), codec);
        Preconditions.checkState(preCodec == null, "The type  %s has been set with class %s", codec.getType(), preCodec);
    }

    /**
     * 是否需要实例帮助反序列化(例如Google proto buffer就需要对象实例反序列化)
     *
     * @param type
     * @return
     */
    public static boolean needInstanceCreaotr(byte type) {
        return type == PROTOBUFF_CODEC.getType();
    }
}
