package com.babyfs.tk.service.biz.service.parambean.internal;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.commons.validator.ValidateResult;
import com.babyfs.tk.service.biz.service.parambean.DataBindException;
import com.babyfs.tk.service.biz.service.parambean.IParamBeanService;
import com.babyfs.tk.service.biz.service.parambean.ITypeConverter;
import com.babyfs.tk.service.biz.service.parambean.annotation.ParamMetaData;
import com.babyfs.tk.service.biz.service.validator.IValidateService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * 请求参数验证服务
 * <p/>
 */
public class ParamBeanServiceImpl implements IParamBeanService {
    /**
     * 数据校验服务
     */
    @Inject
    private IValidateService validateService;

    /**
     * 保存验证信息元数据的Map
     */
    @Inject
    private Map<Class, List<ParamMetaData>> metaDataMap;

    /**
     * bean接口和对应实现类的Map
     */
    @Inject
    @Named(GlobalKeys.PARAM_BEAN_IMPL_MAP)
    private Map<Class, Class> beanImplMap;

    /**
     * 数据类型转换器
     */
    @Inject
    private ITypeConverter typeConverter;

    @Override
    public <T> T buildParamBean(HttpServletRequest request, Class<T> clazz) throws DataBindException {
        return buildAndValidate(request, clazz, true);
    }

    @Override
    public <T> T buildParamBeanNoValidation(HttpServletRequest request, Class<T> clazz) throws DataBindException {
        return buildAndValidate(request, clazz, false);
    }

    @Override
    public <T> T buildAndValidate(HttpServletRequest request, Class<T> clazz, boolean validate) throws DataBindException {
        Class<? extends T> implClass = beanImplMap.get(clazz);
        Preconditions.checkNotNull(implClass, "can not find associate impl class for interface '%s'", clazz.getName());
        Preconditions.checkNotNull(request, "HttpServletRequest can not be null");
        try {

            // 是否调用需要校验的构造函数
            if (validate) {
                Constructor<? extends T> constructor = implClass.getConstructor(HttpServletRequest.class,
                        IValidateService.class, ITypeConverter.class);
                return constructor.newInstance(request, validateService, typeConverter);
            } else {
                Constructor<? extends T> constructor = implClass.getConstructor(HttpServletRequest.class,
                        ITypeConverter.class);
                return constructor.newInstance(request, typeConverter);
            }
        } catch (InvocationTargetException e) {
            // 如果是数据绑定异常，则抛出，其他异常则转换成RuntimeException
            if (e.getCause() instanceof DataBindException) {
                throw (DataBindException) e.getCause();
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
