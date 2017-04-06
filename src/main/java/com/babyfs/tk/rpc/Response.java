package com.babyfs.tk.rpc;

/**
 * RPC请求的响应
 */
public class Response extends RPC {
    /**
     * 响应的返回结果
     */
    private Object response;
    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errormsg;


    public Response() {

    }

    /**
     * @param id       请求的id号
     * @param response 响应的结果
     * @param success  是否成功
     */
    public Response(int id, Object response, boolean success) {
        this.id = id;
        this.response = response;
        this.success = success;
    }

    public Object getResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response)) return false;
        if (!super.equals(o)) return false;

        Response response1 = (Response) o;

        if (success != response1.success) return false;
        if (response != null ? !response.equals(response1.response) : response1.response != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (success ? 1 : 0);
        return result;
    }
}
