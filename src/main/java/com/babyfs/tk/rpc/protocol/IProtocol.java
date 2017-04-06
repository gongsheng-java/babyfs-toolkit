package com.babyfs.tk.rpc.protocol;

/**
 * 协议转换接口
 *
 * @param <FROM>
 * @param <TO>
 */
public interface IProtocol<FROM, TO> {
    /**
     * 将{@link FROM}类型的数据编码为{@link TO}类型的数据
     *
     * @param from
     */
    public TO encode(FROM from);

    /**
     * 将{@link FROM}类型的数据编码为{@link TO}类型的数据
     *
     * @param from
     * @param to
     */
    public void encode(FROM from, TO to);

    /**
     * 将{@link TO}类型的数据啊解码为{@link FROM}类型的数据
     *
     * @param to
     */
    public FROM decode(TO to);
}
