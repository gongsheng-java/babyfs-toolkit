package com.babyfs.tk.page;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回结构
 * Create by gao.wei on 2019-04-23
 */
public class Pageable<E> implements Serializable {

    public static <T> Pageable<T> of(Pageable.Page page, List<T> items) {
        return new Pageable<>(items, page);
    }

    public static <T> Pageable<T> of(int pageNum, int limit, long totalSize, List<T> items) {
        return new Pageable<>(items, Page.of(pageNum, limit, totalSize));
    }

    private List<E> items = Collections.emptyList();

    private Page page;

    public Pageable() {
    }

    public Pageable(List<E> items, Page page) {
        this.items = items;
        this.page = page;
    }

    public List<E> getItems() {
        return items;
    }

    public void setItems(List<E> items) {
        this.items = items;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public static class Page implements Serializable {

        public static Page of(int pageNum, int limit, long totalSize) {
            Preconditions.checkArgument(pageNum > 0 , "pageNum must gt 0");
            Preconditions.checkArgument(limit > 0 , "limit must gt 0");
            Preconditions.checkArgument(totalSize >= 0 , "totalSize must ge 0");
            long totalPage = limit >= totalSize ? 1 : (totalSize / limit) + (totalSize % limit > 0 ? 1 : 0);
            return new Page(pageNum, limit, totalPage, totalSize);
        }

        private int pageNum;

        private int limit;

        private long totalPage;

        private long totalSize;

        public Page() {
        }

        private Page(int pageNum, int limit, long totalPage, long totalSize) {
            this.pageNum = pageNum;
            this.limit = limit;
            this.totalPage = totalPage;
            this.totalSize = totalSize;
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

        public long getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(long totalPage) {
            this.totalPage = totalPage;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
    }
}
