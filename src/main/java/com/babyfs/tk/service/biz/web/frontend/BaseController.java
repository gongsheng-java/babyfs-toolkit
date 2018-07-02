package com.babyfs.tk.service.biz.web.frontend;


import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.config.IGlobalService;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.validator.ValidateResult;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.validator.IValidateService;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 通用的基础Controller
 */
public abstract class BaseController {
    public static final String PARAM_RESPONSE_TYPE_NAME = "_t";
    /**
     * 全局配置
     */
    @Inject
    protected IGlobalService global;

    @Inject
    protected IValidateService validateService;

    /**
     * 取得{@link IGlobalService#getGlobalConfigService()}在ModelAndView中的Key名词
     *
     * @return
     */
    protected abstract String getModelViewGlobalConfigKey();

    /**
     * 取得模板基本路径的配置Key
     *
     * @return
     */
    protected abstract String getTemplateBaseConfigKey();

    /**
     * 构建基本的ModelAndView
     *
     * @param exceptedType 期望的响应类型,如果不为空,则返回由exceptedType指定的响应类型
     * @param request      从request中检查参数<code>{@value #PARAM_RESPONSE_TYPE_NAME}</code>来判断响应的类型
     * @return
     */
    protected Pair<ModelAndView, HttpResponseType> createModelAndViewPair(HttpResponseType exceptedType, HttpServletRequest request) {
        HttpResponseType responseType = this.getExceptedResponseType(exceptedType, request);
        ModelAndView modelAndView = new ModelAndView();
        if (responseType == HttpResponseType.HTML) {
            Map<String, Object> model = modelAndView.getModel();
            model.put(getModelViewGlobalConfigKey(), global.getGlobalConfigService());
        } else if (responseType == HttpResponseType.JSON) {
            modelAndView.setView(new SimpleFastJsonJsonView());
        } else if (responseType == HttpResponseType.XML) {
            modelAndView.setView(new SimpleXMLView());
        }
        return Pair.of(modelAndView, responseType);
    }


    /**
     * 取得View Template的路径
     *
     * @param template
     * @return
     */
    protected String getViewTemplatePath(String template) {
        String templateRoot = Preconditions.checkNotNull(global.getGlobalConfigService().get(getTemplateBaseConfigKey()));
        return templateRoot + "/" + template + ".jsp";
    }

    /**
     * @param modelAndViewPair
     * @param data
     * @param msg
     * @return
     */
    protected ModelAndView responseFail(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, final Object data, final String msg) {
        ModelAndView modelAndView = modelAndViewPair.first;
        HttpResponseType responseType = modelAndViewPair.second;
        int code = ServiceResponse.FAIL_KEY;
        if (responseType == HttpResponseType.JSON) {
            return responseFailJSON(modelAndView, code, data, msg);
        } else {
            modelAndView.addAllObjects(ResponseUtil.createResponseMap(false, code, data, msg));
            return modelAndView;
        }
    }

    /**
     * @param modelAndViewPair
     * @param data
     * @param msg
     * @return
     */
    protected ModelAndView responseSuccess(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, final Object data, final String msg) {
        return responseSuccess(modelAndViewPair, data, msg, null, null);
    }

    /**
     * @param modelAndViewPair
     * @param data
     * @param msg
     * @param filters
     * @return
     */
    protected ModelAndView responseSuccess(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, final Object data, final String msg, List<SerializeFilter> filters) {
        return responseSuccess(modelAndViewPair, data, msg, null, filters);
    }

    /**
     * @param modelAndViewPair
     * @param data
     * @param msg
     * @param config
     * @param filters
     * @return
     */
    protected ModelAndView responseSuccess(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, final Object data, final String msg, SerializeConfig config, List<SerializeFilter> filters) {
        ModelAndView modelAndView = modelAndViewPair.first;
        HttpResponseType responseType = modelAndViewPair.second;
        if (responseType == HttpResponseType.JSON) {
            return responseSuccessJSON(modelAndView, data, msg, config, filters);
        } else {
            int code = ServiceResponse.SUCCESS_KEY;
            modelAndView.addAllObjects(ResponseUtil.createResponseMap(true, code, data, msg));
            return modelAndView;
        }
    }

    /**
     * JSON格式的错误响应
     *
     * @param modelAndView
     * @param errorMsg
     * @return
     */
    protected ModelAndView responseFailJSON(ModelAndView modelAndView, String errorMsg) {
        return responseFailJSON(modelAndView, ServiceResponse.FAIL_KEY, null, errorMsg);
    }

    /**
     * JSON格式的错误响应
     *
     * @param modelAndView
     * @param code
     * @param data
     * @param msg
     * @return
     */
    protected ModelAndView responseFailJSON(ModelAndView modelAndView, int code, Object data, String msg) {
        return responseJSON(modelAndView, false, code, data, msg, null, null);
    }


    /**
     * JSON格式的成功响应
     *
     * @param modelAndView
     * @param data
     * @param msg
     * @return
     */
    protected ModelAndView responseSuccessJSON(ModelAndView modelAndView, Object data, String msg) {
        return responseSuccessJSON(modelAndView, data, msg, null);
    }

    /**
     * JSON格式的成功响应
     *
     * @param modelAndView
     * @param data
     * @param msg
     * @param serializeFilters
     * @return
     */
    protected ModelAndView responseSuccessJSON(ModelAndView modelAndView, Object data, String msg, List<SerializeFilter> serializeFilters) {
        return responseSuccessJSON(modelAndView, data, msg, null, serializeFilters);
    }

    /**
     * JSON格式的成功响应
     *
     * @param modelAndView
     * @param data
     * @param msg
     * @param serializeConfig
     * @param serializeFilters
     * @return
     */
    protected ModelAndView responseSuccessJSON(ModelAndView modelAndView, Object data, String msg, SerializeConfig serializeConfig, List<SerializeFilter> serializeFilters) {
        return responseJSON(modelAndView, true, ServiceResponse.SUCCESS_KEY, data, msg, serializeConfig, serializeFilters);
    }

    /**
     * JSON 响应
     *
     * @param modelAndView
     * @param success
     * @param code
     * @param data
     * @param msg
     * @param serializeConfig
     * @param serializeFilters
     * @return
     */
    protected ModelAndView responseJSON(ModelAndView modelAndView, boolean success, int code, Object data, String msg, SerializeConfig serializeConfig, List<SerializeFilter> serializeFilters) {
        View view = modelAndView.getView();
        if (view == null || !(view instanceof SimpleFastJsonJsonView)) {
            view = new SimpleFastJsonJsonView();
            modelAndView.setView(view);
        }
        SimpleFastJsonJsonView simpleFastJsonJsonView = (SimpleFastJsonJsonView) view;
        simpleFastJsonJsonView.setFilters(serializeFilters);
        simpleFastJsonJsonView.setConfig(serializeConfig);
        simpleFastJsonJsonView.setModel(ResponseUtil.createResponseMap(success, code, data, msg));
        return modelAndView;
    }

    /**
     * 根据service response输出响应
     *
     * @param modelAndViewPair
     * @param serviceResponse
     * @return
     */
    protected ModelAndView responseService(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, ServiceResponse serviceResponse) {
        return responseService(modelAndViewPair, serviceResponse, null, null);
    }

    /**
     * 根据service response输出响应
     *
     * @param modelAndViewPair
     * @param serviceResponse
     * @param serializeConfig
     * @param serializeFilters
     * @return
     */
    protected ModelAndView responseService(final Pair<ModelAndView, HttpResponseType> modelAndViewPair, ServiceResponse serviceResponse, SerializeConfig serializeConfig, List<SerializeFilter> serializeFilters) {
        if (serviceResponse.isSuccess()) {
            return responseSuccess(modelAndViewPair, serviceResponse.getData(), serviceResponse.getMsg(), serializeConfig, serializeFilters);
        } else {
            return responseFail(modelAndViewPair, serviceResponse.getData(), serviceResponse.getMsg());
        }
    }

    /**
     * 根据验证规则验证参数
     *
     * @param ruleAndParams 规则名称和参数值数组,次序为 ruleName1,paramValue1,ruleName2,paramValue2..
     * @return 返回验证结果
     */
    protected ValidateResult validateParamByRule(String... ruleAndParams) {
        Preconditions.checkNotNull(validateService, "Invalid validateService");
        Preconditions.checkNotNull(ruleAndParams, "Can't valiad null params");
        Preconditions.checkArgument(ruleAndParams.length % 2 == 0, "The length of params must be even ");
        for (int i = 0; i < ruleAndParams.length; i += 2) {
            String ruleName = ruleAndParams[i];
            String value = ruleAndParams[i + 1];
            ValidateResult result = validateService.validate(ruleName, value);
            if (result == null || !result.isSuccess()) {
                return result;
            }
        }
        return ValidateResult.RESULT_OK;
    }


    /**
     * 取得当前请求的响应类型
     *
     * @param exceptedType 期望的响应类型,
     * @param request
     * @return 如果<code>exceptedType</code>不为空,则返回由exceptedType指定的响应类型;
     * 否则从<code>request</code>中检查参数<code>{@value #PARAM_RESPONSE_TYPE_NAME}</code></code>来判断响应的类型
     */
    protected HttpResponseType getExceptedResponseType(HttpResponseType exceptedType, final HttpServletRequest request) {
        if (exceptedType != null) {
            return exceptedType;
        }
        if ("json".equalsIgnoreCase(request.getParameter(PARAM_RESPONSE_TYPE_NAME))) {
            return HttpResponseType.JSON;
        } else {
            return HttpResponseType.HTML;
        }
    }


    /**
     * 设置消息结果
     *
     * @param modleAndViewPair
     */
    protected void fillMsgView(Pair<ModelAndView, HttpResponseType> modleAndViewPair) {
        if (modleAndViewPair.getSecond() == HttpResponseType.HTML) {
            modleAndViewPair.getFirst().setViewName(getViewTemplatePath("common/msg"));
        }
    }

    /**
     * @param name
     * @param value
     * @return
     */
    protected Map<String, Object> createDataMap(String name, Object value) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(name, value);
        return map;
    }

    /**
     * 设置响应的模板及其参数
     *
     * @param modelAndViewPair
     * @param title
     * @param template
     * @param subTemplates
     */
    @SafeVarargs
    protected final void fillTemplateView(Pair<ModelAndView, HttpResponseType> modelAndViewPair, String title, String template, Pair<String, String>... subTemplates) {
        ModelAndView modelAndView = modelAndViewPair.getFirst();
        modelAndView.addObject("title", title);
        modelAndView.setViewName(getViewTemplatePath(template));
        if (subTemplates != null) {
            for (Pair<String, String> subTemplate : subTemplates) {
                modelAndView.addObject(subTemplate.first, getViewTemplatePath(subTemplate.second));
            }
        }
    }

    /**
     * 设置重定向的Veiw
     *
     * @param modleAndViewPair
     * @param redirectURL
     */
    protected void fillRedirectView(Pair<ModelAndView, HttpResponseType> modleAndViewPair, String redirectURL) {
        modleAndViewPair.first.setView(new RedirectView(redirectURL, false, true, false));
    }

    protected String getStringParameter(HttpServletRequest request, String name) {
        return StringUtils.trimToNull(ServletRequestUtils.getStringParameter(request, name, null));
    }

    protected int getIntParameter(HttpServletRequest request, String name) {
        return ServletRequestUtils.getIntParameter(request, name, 0);
    }

    protected long getLongParameter(HttpServletRequest request, String name) {
        return ServletRequestUtils.getLongParameter(request, name, 0L);
    }

    protected double getDoubleParameter(HttpServletRequest request, String name) {
        return ServletRequestUtils.getDoubleParameter(request, name, 0.0);
    }

    protected boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultVal) {
        return ServletRequestUtils.getBooleanParameter(request, name, defaultVal);
    }

}
