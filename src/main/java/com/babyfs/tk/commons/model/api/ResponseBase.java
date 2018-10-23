package com.babyfs.tk.commons.model.api;

public class ResponseBase {
    private int code;
    private String msg;
    private boolean success;

    public ResponseBase() {
        this.success = true;
    }

    public ResponseBase(int code, String message) {
        this();
        this.code = code;
        this.msg = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static ResponseBase returnBaseResponse(int code, String message) {
        return new ResponseBase(code, message);
    }
}
