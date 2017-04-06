package com.babyfs.tk.commons.model;

import java.io.Serializable;
import java.util.List;

/**
 * 基础的列表类数据返回类型
 * <p/>
 */
public class BaseListResData<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -7233468849920570516L;

    /** 下一页游标 */
    private long nextCursor = 0L;

    /** 上一页游标 */
    private long preCursor = 0L;

    /** 总数 */
    private int count = 0;

    /** 数据列表 */
    private List<T> datas;

    public BaseListResData(List<T> datas, int count, long preCursor, long nextCursor){
        this.count = count;
        this.datas = datas;
        this.preCursor = preCursor;
        this.nextCursor = nextCursor;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(long nextCursor) {
        this.nextCursor = nextCursor;
    }

    public long getPreCursor() {
        return preCursor;
    }

    public void setPreCursor(long preCursor) {
        this.preCursor = preCursor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
