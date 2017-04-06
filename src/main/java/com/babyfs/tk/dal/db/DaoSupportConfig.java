package com.babyfs.tk.dal.db;

/**
 * {@link DaoSupport}的配置类,用于配置JDBC相关的参数
 */
public class DaoSupportConfig {
    /**
     * 查询超时,单位秒
     */
    private int queryTimeout;
    /**
     * 批量取结果的条数
     */
    private int fetchSize;

    public DaoSupportConfig() {
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}
