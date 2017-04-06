package com.babyfs.tk.commons.json;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

/**
 * JSON 工具类
 */
public final class JSONUtils {
    private JSONUtils() {
    }

    /**
     * 从<code>config</code>加载配置
     *
     * @param config
     * @return
     * @throws IOException
     */
    public static JSONObject loadConfig(String config) {
        URL resource = Resources.getResource(config);
        try {
            String configContent = Resources.asCharSource(resource, Charsets.UTF_8).read();
            return JSONObject.parseObject(configContent);
        } catch (IOException e) {
            throw new RuntimeException("Can't read content from " + config, e);
        }
    }
}
