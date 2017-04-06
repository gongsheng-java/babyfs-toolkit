package com.babyfs.tk.service.biz.service.parambean.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.babyfs.tk.commons.validator.ValidateResult;
import com.babyfs.tk.service.biz.service.parambean.DataBindException;
import com.babyfs.tk.service.biz.service.parambean.ITypeConverter;
import com.babyfs.tk.service.biz.service.parambean.annotation.ParamMetaData;
import com.babyfs.tk.service.biz.service.validator.IValidateService;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * bean接口实现类生成器
 * <p/>
 */
public final class ParamBeanImplGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamBeanImplGenerator.class);

    /**
     * Bean接口和生成的实现类的Map
     */
    private final ImmutableMap<Class, Class> beanImplMap;
    /**
     * Javassist的类池
     */
    private final ClassPool pool = new ClassPool();
    /**
     * Javassist使用的ClassLoader集合
     */
    private final Set<ClassLoader> poolClassLoaders = Sets.newHashSet();

    /**
     * 构造函数
     *
     * @param interfaces 需要生成实现类的接口数组
     * @throws Exception
     */
    public ParamBeanImplGenerator(Class[] interfaces) throws Exception {
        beanImplMap = ImmutableMap.copyOf(initBeanImplMap(interfaces));
    }

    /**
     * 返回生成的Bean接口和实现类的Map
     *
     * @return
     */
    public Map<Class, Class> getBeanImplMap() {
        return beanImplMap;
    }

    /**
     * 从Javassist类池中查找指定的Class，返回相应的CtClass对象
     *
     * @param clazz 想要查找的类
     * @return clazz对应的CtClass对象
     */
    private CtClass resolve(Class clazz) {
        synchronized (poolClassLoaders) {
            try {
                // 如果当前类所在的ClassLoader不在Javassist类池的路径当中，则添加进去
                final ClassLoader loader = clazz.getClassLoader();
                if (loader != null && !poolClassLoaders.contains(loader)) {
                    poolClassLoaders.add(loader);
                    pool.appendClassPath(new LoaderClassPath(loader));
                }
                return pool.get(clazz.getName());
            } catch (NotFoundException e) {
                throw new RuntimeException("Unable to find class " + clazz.getName() + " in default Javassist class pool and loaders " + poolClassLoaders, e);
            }
        }
    }

    /**
     * 初始化Bean接口和实现类的Map
     *
     * @param interfaces 需要实现的接口数组
     * @return 接口和实现类的Map
     * @throws Exception
     */
    private Map<Class, Class> initBeanImplMap(Class[] interfaces) throws Exception {
        pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        Map<Class, Class> map = new HashMap<Class, Class>(interfaces.length);
        for (Class iface : interfaces) {
            if (map.containsKey(iface))
                continue;
            map.put(iface, generateImpl(iface));
        }
        return map;
    }

    /**
     * 根据传入的接口，生成对应的实现类
     *
     * @param interfaceClazz 接口类
     * @return 生成的实现类
     * @throws CannotCompileException
     * @throws NotFoundException
     * @throws ClassNotFoundException
     */
    private Class generateImpl(Class interfaceClazz) throws CannotCompileException, NotFoundException, ClassNotFoundException {
        String interfaceName = interfaceClazz.getName();
        String implClassName = interfaceName + "$Impl";
        LOGGER.info("Generate bean impl class {} for interface {}", implClassName, interfaceName);

        CtClass classInterface = resolve(interfaceClazz);
        CtClass implClass = pool.makeClass(implClassName);
        if (classInterface.isInterface()) {
            implClass.addInterface(classInterface);
        } else {
            implClass.setSuperclass(classInterface);
        }

        CtMethod[] methods = classInterface.getMethods();
        final Map<CtField, ParamMetaData> metaDataMap = new HashMap<CtField, ParamMetaData>();
        for (CtMethod method : methods) {
            ParamMetaData metaData = (ParamMetaData) method.getAnnotation(ParamMetaData.class);
            if (metaData == null)
                continue;
            String methodName = method.getName();

            Preconditions.checkArgument(methodName.startsWith("set"), "method '%s' of interface '%s' must start with set", methodName, interfaceName);
            Preconditions.checkArgument(method.getParameterTypes().length == 1, "method '%s' of interface '%s' must accept just one parameter", methodName, interfaceName);
            Preconditions.checkArgument(CtClass.voidType.equals(method.getReturnType()), "method '%s' of interface '%s' must has a void return type", methodName, interfaceName);

            // 生成字段名称
            int fieldIndex = "set".length();
            String fieldName = Character.toLowerCase(methodName.charAt(fieldIndex)) + methodName.substring(fieldIndex + 1);
            CtField field = new CtField(method.getParameterTypes()[0], fieldName, implClass);
            implClass.addField(field);
            implClass.addMethod(generateGetter(implClass, field));
            implClass.addMethod(generateSetter(implClass, field));

            metaDataMap.put(field, metaData);
        }
        implClass.addConstructor(generateConstructor(implClass, metaDataMap, true));
        implClass.addConstructor(generateConstructor(implClass, metaDataMap, false));
        Class impl = implClass.toClass();
        implClass.detach();

        // 检查生成的实现类对接口的实现情况
        if (interfaceClazz.isInterface()) {
            checkMethodImplementation(interfaceClazz, impl);
        }

        return impl;
    }

    /**
     * 检查实现类对接口定义的所有方法的实现情况
     *
     * @param ifClass   接口类
     * @param implClass 实现类
     */
    private void checkMethodImplementation(Class ifClass, Class implClass) {
        Preconditions.checkNotNull(ifClass);
        Preconditions.checkNotNull(implClass);
        try {
            Method[] methods = ifClass.getMethods();
            for (Method m : methods) {
                Method m1 = implClass.getMethod(m.getName(), m.getParameterTypes());
                Preconditions.checkArgument(m1.getDeclaringClass() != ifClass, "method '%s' of interface '%s' does not implemented", m.getName(), ifClass.getName());
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("check bean impl class error", e);
        }
    }

    /**
     * 生成构造方法
     *
     * @param cc          CtClass对象
     * @param metaDataMap 字段和对应元数据的Map
     * @param validate    是否生成校验
     * @return 构造方法
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private CtConstructor generateConstructor(CtClass cc, Map<CtField, ParamMetaData> metaDataMap, boolean validate)
            throws CannotCompileException, NotFoundException {
        StringBuilder builder = new StringBuilder();
        final String bindExceptionName = DataBindException.class.getName();
        if (validate) {
            builder.append(String.format("public %s(javax.servlet.http.HttpServletRequest request, %s validate, %s converter) throws %s {",
                    cc.getSimpleName(), IValidateService.class.getName(), ITypeConverter.class.getName(), bindExceptionName));
        } else {
            builder.append(String.format("public %s(javax.servlet.http.HttpServletRequest request, %s converter) throws %s {",
                    cc.getSimpleName(), ITypeConverter.class.getName(), bindExceptionName));
        }
        for (Map.Entry<CtField, ParamMetaData> entry : metaDataMap.entrySet()) {
            final CtField field = entry.getKey();
            final ParamMetaData metaData = entry.getValue();
            builder.append("{");
            builder.append(String.format("final String param = \"%s\";", metaData.paramName()));
            builder.append("String value = request.getParameter(param);");
            if (validate) {
                builder.append(String.format("%s res = validate.validate(\"%s\", value);", ValidateResult.class.getName(), metaData.rule()));
                builder.append(String.format("if (!res.isSuccess()) throw new %s(param, res.getErrorMsg());", bindExceptionName));
            }
            builder.append(String.format("if (value == null || value.length() == 0) value = \"%s\";", metaData.defaultValue()));
            String fieldTypeName = field.getType().getName();
            builder.append(String.format("try {this.%s=(%s)converter.convert(%s.class, value);} catch (Exception e) {throw new %s(param, e);}",
                    field.getName(), fieldTypeName, fieldTypeName, bindExceptionName));
            builder.append("}");
        }
        builder.append("}");

        return CtNewConstructor.make(builder.toString(), cc);
    }

    /**
     * 生成字段的Getter方法
     *
     * @param cc    需要生成Getter方法的CtClass对象
     * @param field Getter方法对应的字段
     * @return 描述Getter方法的CtMethod对象
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private CtMethod generateGetter(CtClass cc, CtField field) throws NotFoundException, CannotCompileException {
        String fieldName = field.getName();
        String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        CtMethod getter = new CtMethod(field.getType(), methodName, null, cc);
        getter.setBody(String.format("return this.%s;", fieldName));
        return getter;
    }

    /**
     * 生成字段的Setter方法
     *
     * @param cc    需要生成Setter方法的CtClass对象
     * @param field Setter方法对应的字段
     * @return 描述Setter方法的CtMethod对象
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private CtMethod generateSetter(CtClass cc, CtField field) throws NotFoundException, CannotCompileException {
        String fieldName = field.getName();
        String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        CtMethod setter = new CtMethod(CtClass.voidType, methodName, new CtClass[]{field.getType()}, cc);
        setter.setBody(String.format("this.%s = $1;", fieldName));
        return setter;
    }
}
