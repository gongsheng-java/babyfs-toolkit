package com.babyfs.tk.galaxy.demo;


import com.babyfs.tk.commons.model.ServiceResponse;

import java.util.List;
import java.util.Map;


public interface Health {

    ServiceResponse<ExcelModel> jobHealth(Map<String, Object> queryMap);

    ServiceResponse<PostModel> login(List list);

    ServiceResponse<String> JsonTest(String string);

    ServiceResponse<PostModel> notJsonTest(Long id);
}
