package com.babyfs.tk.commons.model.api;

import com.babyfs.tk.commons.model.ServiceResponse;

public class Response<T> extends ResponseBase {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Response<T> returnResponse(T data) {
        Response<T> response = new Response<T>();
        response.setData(data);
        return response;
    }

    public static <T> Response<T> returnFailResponse(int code, String message) {
        Response<T> response = new Response<T>();
        response.setCode(code);
        response.setMsg(message);
        response.setSuccess(false);
        return response;
    }

    public static <T> Response<T> fromServiceResponse(ServiceResponse<T> serviceResponse) {
        Response<T> response = new Response<T>();
        response.setData(serviceResponse.getData());
        response.setMsg(serviceResponse.getMsg());
        response.setCode(serviceResponse.getCode());
        response.setSuccess(serviceResponse.isSuccess());
        return response;
    }
}
