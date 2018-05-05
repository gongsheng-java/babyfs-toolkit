package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.commons.model.ServiceResponse;

import java.util.Map;

public class BadServiceImpl implements BadService {
    @Override
    public ServiceResponse<ExcelModel> bad(Map<String, Object> queryMap) {
        return null;
    }

    @Override
    public ServiceResponse<ExcelModel> exception(Map<String, Object> queryMap) {
        throw new RuntimeException("it'ok,this exception is test");
    }
}
