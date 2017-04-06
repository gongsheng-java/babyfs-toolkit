package com.babyfs.tk.service.biz.service.parambean.internal;


import com.babyfs.tk.service.biz.service.parambean.annotation.ParamMetaData;

/**
 * 测试用接口bean
 * <p/>
 */
public interface ITestBean {
    @ParamMetaData(paramName = "i", rule = "requiredId")
    void setInteger(Integer i);

    Integer getInteger();

    @ParamMetaData(paramName = "s", rule = "notEmpty")
    void setString(String s);

    String getString();

    @ParamMetaData(paramName = "os", rule = "optionalEmail", defaultValue = "jiahao.fang@renren-inc.com")
    void setOptionalEmail(String s);

    String getOptionalEmail();

    @ParamMetaData(paramName = "oi", rule = "optionalInt", defaultValue = "2")
    void setOptionalInt(Integer i);

    Integer getOptionalInt();

//    void testfunc();
}
