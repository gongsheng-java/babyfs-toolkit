package com.babyfs.tk.service.biz.base.query;

import java.util.Date;

/**
 * 基础的查询参数
 */
public class BaseQuery {
    /**
     * id
     */
    private long id;
    /**
     * 状态,初始值为-1,表示不区分状态
     */
    private byte stat = -1;
    /**
     * 删除标志,初始值为-1,表示不区分删除标志
     */
    private byte del = -1;
    /**
     * 开始时间戳
     */
    private Date start;
    /**
     * 结束时间戳
     */
    private Date end;
    /**
     * 只按照id排序
     */
    private boolean sortOnlyById;
    /**
     * 是否是升序,默认为降序
     */
    private boolean ascend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getStat() {
        return stat;
    }

    public void setStat(byte stat) {
        this.stat = stat;
    }

    public byte getDel() {
        return del;
    }

    public void setDel(byte del) {
        this.del = del;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isSortOnlyById() {
        return sortOnlyById;
    }

    public void setSortOnlyById(boolean sortOnlyById) {
        this.sortOnlyById = sortOnlyById;
    }

    public boolean isAscend() {
        return ascend;
    }

    public void setAscend(boolean ascend) {
        this.ascend = ascend;
    }
}
