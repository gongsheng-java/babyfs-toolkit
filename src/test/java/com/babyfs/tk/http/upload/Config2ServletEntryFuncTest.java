package com.babyfs.tk.http.upload;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.json.JSONUtils;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.http.upload.config.Config2ServletEntryFunc;
import org.junit.Test;

public class Config2ServletEntryFuncTest {

    @Test
    public void testApply() throws Exception {
        JSONObject jsonObject = JSONUtils.loadConfig("upload-init-config.json");
        JSONArray jsonArray = jsonObject.getJSONArray("servlets");
        ListUtil.transform(jsonArray, new Config2ServletEntryFunc());
    }
}
