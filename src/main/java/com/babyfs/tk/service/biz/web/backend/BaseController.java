package com.babyfs.tk.service.biz.web.backend;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.config.IGlobalService;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.op.user.ResultCodeConst;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 基础Controller
 */
public abstract class BaseController {
    /**
     * 参数错误的响应结果
     */
    protected static final ServiceResponse BAD_PARAM_RESPONSE = ServiceResponse.createFailResponse(ResultCodeConst.PARAMETER_ERROR, "参数错误");
    /**
     * 服务处理异常
     */
    protected static final ServiceResponse SERVICE_ERROR_RESPONSE = ServiceResponse.createFailResponse(ResultCodeConst.FAULT, "服务异常");

    @Inject
    protected IGlobalService globalService;

    /**
     * 构建参数错误的响应
     *
     * @param msg 错误描述
     * @return
     */
    protected ServiceResponse paramErrorResponse(String msg) {
        return new ServiceResponse(false, ResultCodeConst.PARAMETER_ERROR, null, msg);
    }

    /**
     * 填充基础的数据
     *
     * @param modelAndView
     */
    protected void fillBaseModle(ModelAndView modelAndView) {
        Preconditions.checkNotNull(modelAndView);
        modelAndView.addObject("global", this.buildGlobalConfig());
    }

    protected void writeErrorResonse(HttpServletResponse response) {
        ResponseUtil.writeJSONResult(response, SERVICE_ERROR_RESPONSE, null);
    }

    private Map<String, String> buildGlobalConfig() {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(5);
        return map;
    }
}
