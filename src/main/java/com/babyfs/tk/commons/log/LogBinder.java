package com.babyfs.tk.commons.log;

import com.babyfs.tk.commons.log.annotation.ServiceLog;
import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;

/*
* 日志拦截器
* */
public class LogBinder {
    private final static String ScanPackage = "com.babyfs.service";

    public static void bindLogInterceptor(Binder binder) {
        binder.bindInterceptor(
                Matchers.inSubpackage(ScanPackage).and(Matchers.annotatedWith(ServiceLog.class)),
                Matchers.annotatedWith(ServiceLog.class),
                new ServiceLogInterceptor()
        );
    }
}
