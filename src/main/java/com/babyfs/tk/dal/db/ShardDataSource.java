package com.babyfs.tk.dal.db;

import com.google.common.base.Preconditions;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.db.shard.ShardDataSourceContainer;
import com.babyfs.tk.dal.db.shard.ShardUtil;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * shard的DataSource
 */
public class ShardDataSource implements DataSource {
    private final ShardDataSourceContainer shardSourceContainer;
    private final DataSource defaultDataSource;
    private final String defaultShardGroupId;
    private final String defaultShardId;

    /**
     * @param shardSourceContainer shard数据源容器
     * @param defaultDataSource    默认的数据源
     */
    public ShardDataSource(@Nonnull ShardDataSourceContainer shardSourceContainer, DataSource defaultDataSource) {
        this.shardSourceContainer = shardSourceContainer;
        this.defaultDataSource = defaultDataSource;
        this.defaultShardGroupId = null;
        this.defaultShardId = null;
    }

    /**
     * @param shardSourceContainer
     * @param defaultShardGroupId
     * @param defaultShardId
     */
    public ShardDataSource(@Nonnull ShardDataSourceContainer shardSourceContainer, String defaultShardGroupId, String defaultShardId) {
        this.shardSourceContainer = shardSourceContainer;
        this.defaultDataSource = null;
        this.defaultShardGroupId = defaultShardGroupId;
        this.defaultShardId = defaultShardId;

    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("ShardDataSource has not suppor this operation.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        Preconditions.checkArgument(iface != null, "Interface argument must not be null");
        if (!DataSource.class.equals(iface)) {
            throw new SQLException("DataSource of type [" + getClass().getName() +
                    "] can only be unwrapped as [javax.sql.DataSource], not as [" + iface.getName());
        }
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return DataSource.class.equals(iface);
    }

    /**
     * 根据ThreadLocal中的lookup key取得DataSource
     *
     * @return
     * @throws SQLException
     */
    protected DataSource determineDataSource() throws SQLException {
        Pair<String, String> lookKey = ShardUtil.getLookKey();
        if (lookKey == null) {
            throw new SQLException("Can't find the lookup key");
        }
        if (lookKey == ShardUtil.NULL_LOOKUP_KEY) {
            if (this.defaultDataSource != null) {
                return this.defaultDataSource;
            } else if (this.defaultShardGroupId != null && this.defaultShardId != null) {
                return shardSourceContainer.getDataSource(defaultShardGroupId, defaultShardId);
            } else {
                throw new SQLException("Match NULL_LOOKUP_KEY,but the defaultDataSource is null.");
            }
        }
        return shardSourceContainer.getDataSource(lookKey.first, lookKey.second);
    }

}
