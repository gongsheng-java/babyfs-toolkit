package com.babyfs.tk.dal.db.shard.impl;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.db.shard.IDataSourceCreator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.util.List;


/**
 * 使用Tomcat DB Pool 实现的数据源
 */
public class TomcatDataSourceCreator implements IDataSourceCreator {
    @Override
    public javax.sql.DataSource create(String ip, int port, String user, String password, String schema, List<Pair<String, String>> paramters) {
        PoolProperties p = new PoolProperties();
        if (ListUtil.isNotEmpty(paramters)) {
            p.setUrl(MySQLUtil.buildMySQLURL(ip, port, schema, paramters));
        } else {
            p.setUrl(MySQLUtil.buildMySQLURL(ip, port, schema));
        }
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setUsername(user);
        p.setPassword(password);
        p.setJmxEnabled(false);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(1);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        return new org.apache.tomcat.jdbc.pool.DataSource(p);
    }

    @Override
    public void shutdown(javax.sql.DataSource dataSource) {
        Preconditions.checkArgument(dataSource instanceof DataSource, "Tomcat only support " + DataSource.class);
        DataSource tomcatDataSource = (DataSource) dataSource;
        tomcatDataSource.close();
    }
}
