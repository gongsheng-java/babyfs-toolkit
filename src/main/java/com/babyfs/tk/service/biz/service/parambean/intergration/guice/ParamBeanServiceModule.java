package com.babyfs.tk.service.biz.service.parambean.intergration.guice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import com.google.inject.util.Types;
import com.babyfs.tk.commons.GlobalKeys;
import com.babyfs.tk.commons.guice.GuiceKeys;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.service.biz.factory.StaticModuleFactory;
import com.babyfs.tk.service.biz.service.parambean.IParamBeanService;
import com.babyfs.tk.service.biz.service.parambean.ITypeConverter;
import com.babyfs.tk.service.biz.service.parambean.annotation.ParamMetaData;
import com.babyfs.tk.service.biz.service.parambean.internal.DefaultTypeConverterImpl;
import com.babyfs.tk.service.biz.service.parambean.internal.ParamBeanImplGenerator;
import com.babyfs.tk.service.biz.service.parambean.internal.ParamBeanServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求参数bean的Module
 * <p/>
 * 该服务依赖于{@link StaticModuleFactory#BASE_MODULE_VALIDATE_SERVICE}
 * <p/>
 * 使用该服务时，需通过
 * bind(Class[].class).annotatedWith(Names.named(GlobalKeys.PARAM_BEAN_CLASSES)).toInstance(接口类数组)
 * 来注入需要进行处理的所有接口类
 * <p/>
 */
public class ParamBeanServiceModule extends ServiceModule {
    @Override
    protected void configure() {
        bind(GuiceKeys.<Map<Class, List<ParamMetaData>>>getSimpleKey(Map.class, Class.class,
                Types.newParameterizedType(List.class, ParamMetaData.class)))
                .toProvider(ParamMataDataProvider.class).asEagerSingleton();
        bindService(IParamBeanService.class, ParamBeanServiceImpl.class);
        bind(GlobalKeys.PARAM_BEAN_IMPL_MAP_KEY).toProvider(BeanImplMapProvider.class).asEagerSingleton();
        bind(ITypeConverter.class).to(DefaultTypeConverterImpl.class).asEagerSingleton();
    }

    /**
     * 生成接口和对应的实现类的Map的Provider
     */
    private static class BeanImplMapProvider implements Provider<Map<Class, Class>> {
        private static final Logger LOGGER = LoggerFactory.getLogger(BeanImplMapProvider.class);
        /**
         * 具有请求参数元数据配置的Bean接口类数组
         */
        @Inject
        @Named(GlobalKeys.PARAM_BEAN_CLASSES)
        private Class[] classes;

        @Override
        public Map<Class, Class> get() {
            try {
                return new ParamBeanImplGenerator(classes).getBeanImplMap();
            } catch (Exception e) {
                LOGGER.error("generate bean impl class error", e);
                return null;
            }
        }
    }

    private static class ParamMataDataProvider implements Provider<Map<Class, List<ParamMetaData>>> {
        /**
         * 具有请求参数元数据配置的Bean类数组
         */
        @Inject
        @Named(GlobalKeys.PARAM_BEAN_CLASSES)
        private Class[] classes;

        @Override
        public Map<Class, List<ParamMetaData>> get() {
            Preconditions.checkArgument(classes != null);
            Map<Class, List<ParamMetaData>> map = new HashMap<Class, List<ParamMetaData>>();
            for (Class clazz : classes) {
                Method[] methods = clazz.getMethods();
                List<ParamMetaData> metaDatas = new ArrayList<ParamMetaData>(methods.length);
                for (Method method : methods) {
                    if (method.getParameterTypes().length != 1) {
                        continue;
                    }
                    ParamMetaData metaData = method.getAnnotation(ParamMetaData.class);
                    if (metaData == null) {
                        continue;
                    }
                    metaDatas.add(metaData);
                }
                map.put(clazz, ImmutableList.copyOf(metaDatas));
            }
            return ImmutableMap.copyOf(map);
        }
    }
}
