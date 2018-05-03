package com.babyfs.tk.galaxy;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * rpc调用exception
 */
public class RpcException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the reason for the failure.
     */
    public RpcException(String message) {
        super(message);
    }

    /**
     * @param message possibly null reason for the failure.
     * @param cause   the cause of the error.
     */
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
