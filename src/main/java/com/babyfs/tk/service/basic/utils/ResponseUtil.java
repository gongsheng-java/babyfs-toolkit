package com.babyfs.tk.service.basic.utils;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import com.babyfs.tk.commons.model.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * response 工具类
 */
public class ResponseUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtil.class.getName());
    public static final String REP_KEY_SUCCESS = "success";
    public static final String REP_KEY_CODE = "code";
    public static final String REP_KEY_MSG = "msg";
    public static final String REP_KEY_DATA = "data";
    public static final String X_CACHE_CONTROL = "X-" + HttpHeaders.CACHE_CONTROL;

    /**
     * 将serviceResponse转为JSON结果写入httpResponse
     *
     * @param httpResponse
     * @param serviceResponse
     * @param filters
     * @return
     */
    public static boolean writeJSONResult(HttpServletResponse httpResponse, ServiceResponse serviceResponse, List<SerializeFilter> filters) {
        return writeJSONResult(httpResponse, serviceResponse, null, filters);
    }

    /**
     * 将serviceResponse转为JSON结果写入httpResponse
     *
     * @param httpResponse
     * @param serviceResponse
     * @param config          JSON序列化配置
     * @param filters         JSON属性过列表
     * @return 成功返回 true,失败返回false.
     */
    public static boolean writeJSONResult(HttpServletResponse httpResponse, ServiceResponse serviceResponse, SerializeConfig config, List<SerializeFilter> filters) {
        Preconditions.checkNotNull(httpResponse);
        String json = result2Json(serviceResponse, config, filters);
        return writeJsonResult(httpResponse, json);
    }

    /**
     * 将结果转换成json字符串
     *
     * @param responseResult
     * @param filters
     * @return
     */
    public static String result2Json(ServiceResponse responseResult, SerializeConfig config, List<SerializeFilter> filters) {
        Map<String, Object> map = createResponseMap(responseResult);
        return JSONUtil.toJSONString(map, config, filters, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 将{@link ServiceResponse}转换为Map
     *
     * @param serviceResponse
     * @return
     */
    public static Map<String, Object> createResponseMap(ServiceResponse serviceResponse) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(REP_KEY_SUCCESS, serviceResponse.isSuccess());
        map.put(REP_KEY_CODE, serviceResponse.getCode());
        map.put(REP_KEY_MSG, serviceResponse.getMsg());
        map.put(REP_KEY_DATA, serviceResponse.getData());
        return map;
    }

    /**
     * @param success
     * @param code
     * @param data
     * @param msg
     * @return
     */
    public static Map<String, Object> createResponseMap(boolean success, int code, Object data, String msg) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(REP_KEY_SUCCESS, success);
        map.put(REP_KEY_CODE, code);
        map.put(REP_KEY_MSG, msg);
        map.put(REP_KEY_DATA, data);
        return map;
    }

    /**
     * 写json响应到客户端
     * <p>
     * 根据{@value #X_CACHE_CONTROL}是否设置判断{@value HttpHeaders#CACHE_CONTROL}如何设置:
     * 1. 如果X-Cache-Control为null,设置为no-cache
     * 2. 如果X-Cache-Control不为null,且不为空,则设置为X-Cache-Control的值
     * 3. 其他情况不设置Cache-Controle
     *
     * @param response
     * @param json
     * @throws RuntimeException 如果写入失败将抛出异常
     */
    public static boolean writeJsonResult(HttpServletResponse response, String json) {
        try {
            String xCacheControl = response.getHeader(X_CACHE_CONTROL);
            if (xCacheControl == null) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            } else {
                if (!xCacheControl.isEmpty()) {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, xCacheControl);
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print(json);
            writer.flush();
            return true;
        } catch (Exception e) {
            LOGGER.error("writer to response exception.", e);
            return false;
        }
    }
}
