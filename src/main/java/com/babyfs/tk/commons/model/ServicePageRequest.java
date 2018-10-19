package com.babyfs.tk.commons.model;

import com.babyfs.tk.service.biz.base.query.PageParams;

/*
* 分页请求基类
* */
public class ServicePageRequest extends ServiceRequest {
    private PageParams pageParams;

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }
}
