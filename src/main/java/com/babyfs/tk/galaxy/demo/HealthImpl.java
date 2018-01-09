package com.babyfs.tk.galaxy.demo;

import java.util.List;
import java.util.Map;

public class HealthImpl implements Health {
    @Override
    public ExcelModel jobHealth(Map<String, Object> queryMap) {

        ExcelModel excelModel = new ExcelModel();
        excelModel.setCh("ch");
        excelModel.setEn("en");
        return excelModel;
    }

    @Override
    public PostModel login(List list) {

        PostModel postModel = new PostModel();
        postModel.setMessage("i am message");
        return null;
    }

    @Override
    public String JsonTest(String string) {
        return "JsonTest";
    }

    @Override
    public PostModel notJsonTest(Long id) {
        PostModel postModel = new PostModel();
        postModel.setMessage("i am notJsonTest");
        return postModel;
    }
}
