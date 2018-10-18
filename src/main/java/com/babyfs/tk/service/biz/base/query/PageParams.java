package com.babyfs.tk.service.biz.base.query;

import java.io.Serializable;

/**
 * 分页请求的参数
 */
public class PageParams implements Serializable {
    private static final long serialVersionUID = -6451618304432262725L;
    /**
     * 查询的页码
     */
    private int page;

    /**
     * 请求的每页数量
     */
    private int limit;
    /**
     * 游标
     */
    private long nextCursor;

    /**
     * @param page
     * @param limit
     */
    public PageParams(int page, int limit) {
        this(page, limit, 0);
    }

    /**
     * @param page
     * @param limit
     * @param nextCursor
     */
    public PageParams(int page, int limit, long nextCursor) {
        this.page = page;
        this.limit = limit;
        this.nextCursor = nextCursor;
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public int getBeginIndex() {
        return (getPage() - 1) * limit;
    }

    public int getToIndex() {
        return this.getBeginIndex() + limit - 1;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(long nextCursor) {
        this.nextCursor = nextCursor;
    }
}
