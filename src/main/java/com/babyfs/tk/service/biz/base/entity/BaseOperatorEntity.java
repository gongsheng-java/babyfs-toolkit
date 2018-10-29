package com.babyfs.tk.service.biz.base.entity;

import com.babyfs.tk.commons.model.Operator;
import org.elasticsearch.common.Strings;

import javax.persistence.Column;

/*
* 带有operator信息的db基类
* */
public class BaseOperatorEntity extends BaseAutoIdEntity {
    /*
    * 创建人
    * */
    private String ctOperator;

    /*
    * 修改人
    * */
    private String utOperator;


    @Column(name = "ct_operator")
    public String getCtOperator() {
        return ctOperator;
    }

    public void setCtOperator(String ctOperator) {
        this.ctOperator = ctOperator;
    }

    @Column(name = "ut_operator")
    public String getUtOperator() {
        return utOperator;
    }

    public void setUtOperator(String utOperator) {
        this.utOperator = utOperator;
    }

    public void appendCtOperator(Operator operator) {
        if(operator == null || Strings.isNullOrEmpty(operator.getName())) {
            return;
        }

        this.ctOperator = operator.getName();
        if(!Strings.isNullOrEmpty(operator.getIp())) {
            this.ctOperator +=  "|" + operator.getIp();
        }
    }

    public void appendUtOperator(Operator operator) {
        if(operator == null || Strings.isNullOrEmpty(operator.getName())) {
            return;
        }

        this.utOperator = operator.getName();
        if(!Strings.isNullOrEmpty(operator.getIp())) {
            this.utOperator +=  "|" + operator.getIp();
        }
    }
}
