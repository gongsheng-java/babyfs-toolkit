package com.babyfs.tk.dal.db.shard;

import com.babyfs.tk.commons.base.Pair;

import javax.sql.DataSource;
import java.util.List;

/**
 */
public interface IDataSourceCreator {
    /**
     * 根据指定的参数创建一个{@link DataSource}
     *
     * @param ip
     * @param port
     * @param user
     * @param password
     * @param schema
     * @param paramters {@link Pair#first}参数名,{@link Pair#second}参数值
     * @return
     */
    DataSource create(String ip, int port, String user, String password, String schema, List<Pair<String, String>> paramters);

    /**
     * 关闭数据源
     *
     * @param dataSource
     */
    void shutdown(DataSource dataSource);
}
