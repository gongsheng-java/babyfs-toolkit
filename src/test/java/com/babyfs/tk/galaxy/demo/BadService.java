package com.babyfs.tk.galaxy.demo;


import com.babyfs.tk.commons.model.ServiceResponse;

import java.util.List;
import java.util.Map;


public interface BadService {
    ServiceResponse<ExcelModel> bad(Map<String, Object> queryMap);

    ServiceResponse<ExcelModel> exception(Map<String, Object> queryMap);
}
