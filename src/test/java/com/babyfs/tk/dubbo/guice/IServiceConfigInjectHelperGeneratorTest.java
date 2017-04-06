package com.babyfs.tk.dubbo.guice;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

public class IServiceConfigInjectHelperGeneratorTest {

    @Test
    public void testGenerateInjectHelperClass() throws Exception {
        Class hi = ServiceConfigInjectHelperGenerator.generateInjectHelperClass("Hi", DemoService.class, "ok");
        assertNotNull(hi);
        Method[] declaredMethods = hi.getDeclaredMethods();
        for (Method method : declaredMethods) {
            System.out.println(method);
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                System.out.println(annotation);
            }
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (Annotation[] annotation : parameterAnnotations) {
                for (Annotation a : annotation) {
                    System.out.println(a);
                }
            }
        }
        IServiceConfigInjectHelper setter = (IServiceConfigInjectHelper) hi.newInstance();
        assertNotNull(setter);
    }
}