package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.basic.mq.kafka.IKafkaProducer;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.List;
import java.util.Map;

public class HealthImpl implements Health {
    @Override
    public ServiceResponse jobHealth(Map<String, Object> queryMap) {

        ExcelModel excelModel = new ExcelModel();
        excelModel.setCh("ch");
        excelModel.setEn("en");
        return ServiceResponse.createSuccessResponse(excelModel);
    }

    @Override
    public ServiceResponse login(List list) {

        PostModel postModel = new PostModel();
        postModel.setMessage("i am message");
        return ServiceResponse.createSuccessResponse(postModel);
    }

    @Override
    public ServiceResponse jsonTest(String string) {
        return ServiceResponse.createSuccessResponse("JsonTest");
    }

    @Override
    public ServiceResponse notJsonTest(Long id) {
        PostModel postModel = new PostModel();
        postModel.setMessage("i am notJsonTest");
        return ServiceResponse.createSuccessResponse(postModel);
    }
}
