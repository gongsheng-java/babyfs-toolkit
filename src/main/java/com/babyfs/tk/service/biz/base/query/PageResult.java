package com.babyfs.tk.service.biz.base.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页查询参数及结果封装
 */
public final class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 8950587005023808568L;

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 最大页码数
     */
    private int totalPage;

    /**
     * 每页记录数
     */
    private int limit;

    /**
     * 扩展属性
     */
    private Map<String, Object> ext;

    /**
     * 本页的所有记录
     */
    private List<T> items;

    public PageResult(int page, int limit, int totalCount) {
        this(page, limit, totalCount, null);
    }

    public PageResult(int page, int limit, int totalCount, List<T> items) {
        this.page = page;
        this.limit = limit;
        this.totalCount = totalCount;
        this.items = items;
        this.genMaxPageIndex();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public final void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.genMaxPageIndex();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = (page < 1) ? 1 : page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    /**
     * 根据 totalCount , limit 计算 totalPage
     */
    private void genMaxPageIndex() {
        if (limit > 0) {
            totalPage = ((totalCount % limit) == 0 ? (totalCount / limit) : (totalCount / limit + 1));
        }
    }

    /**
     * 复制除了items之外的数据
     *
     * @param <C>
     * @return
     */
    public <C> PageResult<C> of() {
        return new PageResult<>(this.page, this.limit, this.totalCount);
    }

}
