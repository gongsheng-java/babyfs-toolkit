package com.babyfs.tk.galaxy.demo;



import com.babyfs.tk.galaxy.demo.model.ExcelModel;
import com.babyfs.tk.galaxy.demo.model.PostModel;

import java.util.List;
import java.util.Map;


public interface Health {

    ExcelModel jobHealth(Map<String, Object> queryMap);

    PostModel login(List list);

    String JsonTest(String string);

    PostModel notJsonTest(Long id);
}
