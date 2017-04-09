package com.babyfs.tk.service.biz.validator;


import com.babyfs.tk.commons.validator.ValidateResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 数据验证服务接口
 * <p/>
 */
public interface IValidateService {
    /**
     * 验证map中所有字段值是否合法
     *
     * @param map 需要验证的字段名和值
     * @return 验证返回的结果 {@link ValidateResult ValidateResult}
     */
    ValidateResult validate(Map<String, String> map);

    /**
     * 根据传入的字段名称和值验证是否合法
     *
     * @param key   字段名
     * @param value 字段值
     * @return 验证返回的结果 {@link ValidateResult ValidateResult}
     */
    ValidateResult validate(String key, String value);

    /**
     * 验证传入的HttpServletRequest中所有字段的值是否合法
     *
     * @param request 传入的HttpServletRequest对象
     * @return 验证返回的结果 {@link ValidateResult ValidateResult}
     */
    ValidateResult validate(HttpServletRequest request);

    /**
     * 根据验证规则验证参数
     *
     * @param ruleAndParams
     * @return 返回验证结果
     */
    ValidateResult validateParamByRule(String... ruleAndParams);

    /**
     * 根据规则验证参数
     *
     * @param ruleAndParams 规则名称和参数值数组,次序为 ruleName1,paramValue1,ruleName2,paramValue2..
     * @return 验证结果
     */
    ValidateResult validateParamByRule(List<String> ruleAndParams);
}
