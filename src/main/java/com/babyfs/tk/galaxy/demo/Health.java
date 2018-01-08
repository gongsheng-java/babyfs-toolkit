package com.babyfs.tk.galaxy.demo;


import java.util.List;
import java.util.Map;


public interface Health {

    ExcelModel jobHealth(Map<String, Object> queryMap);

    PostModel login(List list);

    String JsonTest(String string);

    PostModel notJsonTest(Long id);
}
