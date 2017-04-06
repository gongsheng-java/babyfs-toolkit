package com.babyfs.tk.commons.codec;

/**
 * 编码器的类型
 */
public final class CodecTypes {
    /**
     * Java内置的序列化编码
     */
    public static final byte JAVA_CODEC = 1;
    /**
     * 由Hessin提供的序列化编码机制
     */
    public static final byte HESSIAN_CODEC = 2;
    /**
     * 由Google Protobuffer提供的序列化编码机制
     */
    public static final byte PROTOBUFF_CODEC = 3;

    /**
     * 由Kryo提供的序列化编码机制
     */
    public static final byte KRYO_CODEC = 4;

    private CodecTypes() {
    }
}
