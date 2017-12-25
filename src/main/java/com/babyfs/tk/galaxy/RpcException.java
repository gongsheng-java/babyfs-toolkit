package com.babyfs.tk.galaxy;


import com.babyfs.tk.galaxy.client.Util;

public class RpcException extends RuntimeException {


    private static final long serialVersionUID = 1L;

    /**
     * @param message the reason for the failure.
     */
    public RpcException(String message) {
        super(Util.checkNotNull(message, "message"));
    }

    /**
     * @param message possibly null reason for the failure.
     * @param cause   the cause of the error.
     */
    public RpcException(String message, Throwable cause) {
        super(message, Util.checkNotNull(cause, "cause"));
    }

}
