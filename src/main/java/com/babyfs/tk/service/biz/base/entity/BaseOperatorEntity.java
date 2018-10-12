package com.babyfs.tk.service.biz.base.entity;

import javax.persistence.Column;

/*
* 带有operator信息的db基类
* */
public class BaseOperatorEntity extends BaseAssignIdEntity {
    /*
    * 创建人
    * */
    @Column(name = "ct_operator")
    private String ctOperator;

    /*
    * 修改人
    * */
    @Column(name = "ut_operator")
    private String utOperator;

    public String getCtOperator() {
        return ctOperator;
    }

    public void setCtOperator(String ctOperator) {
        this.ctOperator = ctOperator;
    }

    public String getUtOperator() {
        return utOperator;
    }

    public void setUtOperator(String utOperator) {
        this.utOperator = utOperator;
    }
}
