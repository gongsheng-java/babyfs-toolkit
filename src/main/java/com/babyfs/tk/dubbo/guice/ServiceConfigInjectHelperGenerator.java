package com.babyfs.tk.dubbo.guice;

import com.alibaba.dubbo.config.ServiceConfig;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.Set;

/**
 * {@link com.alibaba.dubbo.config.ServiceConfig#ref}的注入帮助类生成器
 */
public final class ServiceConfigInjectHelperGenerator {

    private static final ClassPool POOL = new ClassPool();
    private static final Set POOL_CLASS_LOADERS = Sets.newHashSet();

    static {
        POOL.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    }

    private ServiceConfigInjectHelperGenerator() {

    }

    static synchronized Class<? extends IServiceConfigInjectHelper> generateInjectHelperClass(String genClassName, Class injectClass, String injectName) {
        Preconditions.checkNotNull(genClassName);
        Preconditions.checkNotNull(injectClass);
        if (injectName == null) {
            injectName = "";
        }
        try {
            CtClass pre = POOL.getOrNull(genClassName);
            if (pre != null) {
                return pre.toClass();
            }
            CtClass ctClass = POOL.makeClass(genClassName);
            final CtClass serviceConfigCtClass = resolve(ServiceConfig.class);
            StringBuilder code = new StringBuilder();
            {
                //实现ServiceConfigSetter接口
                ctClass.addInterface(POOL.get(IServiceConfigInjectHelper.class.getName()));
                CtField serviceConfigField = new CtField(serviceConfigCtClass, "serviceConfig", ctClass);
                serviceConfigField.setModifiers(Modifier.PRIVATE);
                ctClass.addField(serviceConfigField);

                addLine(code, "public void  setServiceConfig(" + ServiceConfig.class.getName() + " arg)");
                beginBlock(code);
                {
                    addLine(code, "this." + serviceConfigField.getName() + "=arg;");
                }
                endBlock(code);
                CtMethod method = CtMethod.make(code.toString(), ctClass);
                ctClass.addMethod(method);
                code.delete(0, code.length());
            }
            {
                //生成InjectHelper
                addLine(code, "public void setRef(" + injectClass.getName() + " arg)");
                beginBlock(code);
                {
                    addLine(code, "if(this.serviceConfig!=null)");
                    beginBlock(code);
                    addLine(code, "System.out.println(\"setRef=\"+arg);");
                    addLine(code, "this.serviceConfig.setRef(arg);");
                    endBlock(code);
                    addLine(code, "this.serviceConfig=null;");
                }
                endBlock(code);
                CtMethod method = CtMethod.make(code.toString(), ctClass);
                final MethodInfo methodInfo = method.getMethodInfo();
                final ClassFile ccFile = ctClass.getClassFile();
                final ConstPool constpool = ccFile.getConstPool();

                {
                    //给setRef方法添加@Inject注解
                    AnnotationsAttribute injectAttribue = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                    injectAttribue.addAnnotation(new Annotation(Inject.class.getName(), constpool));
                    methodInfo.addAttribute(injectAttribue);
                }

                {
                    //给setRef参数增加@Named注解
                    if (!Strings.isNullOrEmpty(injectName)) {
                        ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);

                        Annotation namedAnnotation = new Annotation(Named.class.getName(), constpool);
                        namedAnnotation.addMemberValue("value", new StringMemberValue(injectName, constpool));
                        Annotation[][] paramArrays = new Annotation[1][1];
                        paramArrays[0][0] = namedAnnotation;
                        parameterAnnotationsAttribute.setAnnotations(paramArrays);
                        method.getMethodInfo().addAttribute(parameterAnnotationsAttribute);
                    }
                }

                ctClass.addMethod(method);
                code.delete(0, code.length());
            }
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException("Compile class for " + genClassName + " error", e);
        } catch (NotFoundException e) {
            throw new RuntimeException("Find clas error", e);
        }
    }

    private static void beginBlock(StringBuilder sb) {
        sb.append("{");
        sb.append("\r\n");
    }

    private static void endBlock(StringBuilder sb) {
        sb.append("}");
        sb.append("\r\n");
    }

    private static void addLine(StringBuilder sb, String code) {
        sb.append(code);
        sb.append("\r\n");
    }

    /**
     * 在类路径中查找已有的Class,转化为CtClass
     *
     * @param clazz
     * @return
     */
    private static CtClass resolve(Class clazz) {
        synchronized (POOL_CLASS_LOADERS) {
            try {
                final ClassLoader loader = clazz.getClassLoader();
                if (loader != null && !POOL_CLASS_LOADERS.contains(loader)) {
                    POOL_CLASS_LOADERS.add(loader);
                    POOL.appendClassPath(new LoaderClassPath(loader));
                }
                return POOL.get(clazz.getName());
            } catch (NotFoundException e) {
                throw new RuntimeException("Unable to find class " + clazz.getName() + " in default Javassist class pool and loaders " + POOL_CLASS_LOADERS, e);
            }
        }
    }
}
