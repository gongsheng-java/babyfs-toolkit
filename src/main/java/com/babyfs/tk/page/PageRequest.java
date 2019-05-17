package com.babyfs.tk.page;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Objects;

/**
 * 分页请求结构
 *
 * getOffset() -> 获得偏移量
 * Create by gao.wei on 2019-04-23
 */
public class PageRequest implements Serializable {

    public static PageRequest of(int pageNum, int limit) {
        return new PageRequest(Page.of(pageNum, limit));
    }

    public PageRequest() {
    }

    public PageRequest(Page page) {
        this.page = page;
    }

    private Page page = Page.of(1, 20);

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public int getPageNum() {
        if (Objects.isNull(page)) {
            return 0;
        }
        return page.getPageNum();
    }

    public int getLimit() {
        if (Objects.isNull(page)) {
            return 0;
        }
        return page.getLimit();
    }

    public int getOffset() {
        if (Objects.isNull(page)) {
            return 0;
        }
        return (page.getPageNum() - 1) * page.getLimit();
    }

    public static class Page implements Serializable {

        private int pageNum;

        private int limit;

        public static Page of(int pageNum, int limit) {
            Preconditions.checkArgument(pageNum > 0, "page number must gt 0");
            Preconditions.checkArgument(limit > 0 , "limit must gt 0");
            return new Page(pageNum, limit);
        }

        /**
         * for json
         */
        public Page() {
        }

        public Page(int pageNum, int limit) {
            this.pageNum = pageNum;
            this.limit = limit;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}

