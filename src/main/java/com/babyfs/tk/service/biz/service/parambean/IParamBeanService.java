package com.babyfs.tk.service.biz.service.parambean;


import com.babyfs.tk.commons.validator.ValidateResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求参数Bean服务接口
 * <p/>
 */
public interface IParamBeanService {
    /**
     * 根据传入的接口类，查询出对应的实现类，并且从request中提取相应参数，用于构造实现类的实例，并完成校验
     * <p/>
     * 该方法直接调用{@link IParamBeanService#buildAndValidate(HttpServletRequest, Class, boolean)}。
     * 是否验证参数 默认为true，即需要校验。
     *
     * @param request http请求对象
     * @param clazz   接口类
     * @return 接口类对应的实例类的实例，并已经从request中加载对应参数值
     * @throws DataBindException 数据绑定异常，在数据校验或类型转换出错时抛出
     */
    <T> T buildParamBean(HttpServletRequest request, Class<T> clazz) throws DataBindException;

    /**
     * 根据传入的接口类，查询出对应的实现类，并且从request中提取相应参数，用于构造实现类的实例，不做校验
     * <p/>
     * 该方法直接调用{@link IParamBeanService#buildAndValidate(HttpServletRequest, Class, boolean)}。
     * 是否验证参数 false，即不需要校验。
     *
     * @param request http请求对象
     * @param clazz   接口类
     * @return 接口类对应的实例类的实例，并已经从request中加载对应参数值
     * @throws DataBindException 数据绑定异常，在数据校验或类型转换出错时抛出
     */
    <T> T buildParamBeanNoValidation(HttpServletRequest request, Class<T> clazz) throws DataBindException;

    /**
     * 根据传入的接口类，查询出对应的实现类，并且从request中提取相应参数，用于构造实现类的实例，并完成校验
     *
     * @param request  http请求对象
     * @param clazz    接口类
     * @param validate 是否生成校验
     * @return 接口类对应的实例类的实例，并已经从request中加载对应参数值
     * @throws DataBindException 数据绑定异常，在数据校验或类型转换出错时抛出
     */
    <T> T buildAndValidate(HttpServletRequest request, Class<T> clazz, boolean validate) throws DataBindException;
}
