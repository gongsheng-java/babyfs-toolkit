package com.babyfs.tk.rpc.protocol;

import com.babyfs.tk.rpc.protocol.impl.BinaryProtocol;

/**
 * 系统内置支持的协议大全
 */
public final class Protocols {
    /**
     * 二进制的RPC协议
     */
    public static final BinaryProtocol BINARY_PROTOCOL = new BinaryProtocol();

    private Protocols() {

    }
}
