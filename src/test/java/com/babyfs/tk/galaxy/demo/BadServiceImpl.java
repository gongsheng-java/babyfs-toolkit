package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BadServiceImpl implements BadService {
    @Inject
    private Health health;

    @Inject
    public BadServiceImpl(@Named("back") ExecutorService be){

    }

    @Override
    public ServiceResponse<ExcelModel> bad(Map<String, Object> queryMap) {
        return null;
    }

    @Override
    public ServiceResponse<ExcelModel> exception(Map<String, Object> queryMap) {
        throw new RuntimeException("it'ok,this exception is test");
    }
}
