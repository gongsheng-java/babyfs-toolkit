package com.babyfs.tk.commons.model.api;

import com.babyfs.tk.service.biz.base.query.PageParams;

/**
 * @Author: Yihuan
 * @Description: 分页请求类型基类
 * @Date: 2018/9/15.
 */
public class RequestPage extends RequestBase {
    private int pageSize;
    private int pageIndex;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public PageParams toPageParams() {
        int page = this.pageIndex;
        int limit = this.pageSize;
        if (page <= 0) {
            page = 1;
        }
        if (limit <= 0) {
            limit = 20;
        }
        return new PageParams(page, limit);
    }
}
