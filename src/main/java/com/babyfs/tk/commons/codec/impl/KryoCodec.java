package com.babyfs.tk.commons.codec.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.codec.CodecTypes;
import com.babyfs.tk.commons.codec.ICodec;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 使用Kryo实现的编码器
 */
public class KryoCodec implements ICodec {
    /**
     * 是否需要兼容性
     *
     * @see {@link com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer}
     */
    private final boolean needCompatible;
    /**
     * 是否必须注册class
     */
    private final boolean needClassRegistered;
    /**
     * 注册的Class,{@link Pair#getFirst()}是类,{@link Pair#getSecond()}是该类的注册序号
     */
    private final List<Pair<? extends Class, Integer>> registeredClassPairList;
    /**
     * 编码初始Buffer大小
     */
    private final int encodeInitBufferSize;
    /**
     * {@link com.esotericsoftware.kryo.Kryo}
     */
    private ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createKryoInstance();
        }
    };

    /**
     * 启用用类序列化兼容,没有注册的class
     */
    public KryoCodec() {
        this(true, false, null, 512);
    }

    /**
     * @param needCompatible          是否需要兼容性
     * @param needClassRegistered     是否必须注册class
     * @param registeredClassPairList 注册的class列表
     * @param encodeInitBufferSize    编码初始的Buffer大小
     */
    public KryoCodec(boolean needCompatible, boolean needClassRegistered, List<Pair<? extends Class, Integer>> registeredClassPairList, int encodeInitBufferSize) {
        this.needCompatible = needCompatible;
        this.needClassRegistered = needClassRegistered;
        this.registeredClassPairList = Lists.newArrayList();
        this.encodeInitBufferSize = encodeInitBufferSize;
        if (registeredClassPairList != null) {
            for (Pair<? extends Class, Integer> pair : registeredClassPairList) {
                this.registeredClassPairList.add(pair);
            }
        }
    }

    @Override
    public byte getType() {
        return CodecTypes.KRYO_CODEC;
    }

    @Override
    public byte[] encode(Object obj) {
        final Kryo kryo = this.kryoThreadLocal.get();
        Output output = new Output(this.encodeInitBufferSize, -1);
        try {
            kryo.writeClassAndObject(output, obj);
            return output.toBytes();
        } finally {
            output.close();
        }
    }

    @Override
    public Object decode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        final Kryo kryo = this.kryoThreadLocal.get();
        Input input = new Input(data);
        try {
            return kryo.readClassAndObject(input);
        } finally {
            input.close();
        }
    }

    @Override
    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }

    private Kryo createKryoInstance() {
        Kryo kryo = new Kryo();
        if (this.needCompatible) {
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        }
        kryo.setRegistrationRequired(this.needClassRegistered);
        for (Pair<? extends Class, Integer> pair : this.registeredClassPairList) {
            kryo.register(pair.getFirst(), pair.getSecond());
        }
        return kryo;
    }
}
