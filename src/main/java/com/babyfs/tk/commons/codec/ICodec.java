package com.babyfs.tk.commons.codec;

/**
 * 数据的编解码器
 */
public interface ICodec {
    /**
     * 编解码器的类型
     *
     * @return
     */
    public byte getType();

    /**
     * 将对象编码为byte数组
     *
     * @param obj
     * @return
     * @throws CodecException
     */
    public byte[] encode(Object obj);

    /**
     * 将byte数组解码为对象
     *
     * @param data
     * @return
     * @throws CodecException
     */
    public Object decode(byte[] data);

    /**
     * 使用指定的实例帮助反序列化
     *
     * @param data
     * @param instanceCreator
     * @return
     * @throws CodecException
     */
    public Object decode(byte[] data, Object instanceCreator);

}
