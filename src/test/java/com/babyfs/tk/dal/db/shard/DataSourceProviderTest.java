package com.babyfs.tk.dal.db.shard;

import com.babyfs.tk.dal.db.shard.impl.TomcatDataSourceCreator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
public class DataSourceProviderTest {
    private static final String db_host = "127.0.0.1";
    public static void main(String[] args) {
    }

    @Test
    @Ignore
    public void testAddDBInstance() throws Exception {
        DBInstance dbInstance_gsns = new DBInstance("db_0",db_host, 3306, "root", "");
        IDataSourceCreator dataSourceCreator = new TomcatDataSourceCreator();
        ShardDataSourceContainer shardDataSourceContainer = new ShardDataSourceContainer(dataSourceCreator);
        shardDataSourceContainer.addDBInstance(dbInstance_gsns);
        try {
            DBInstance dbInstance_remote = new DBInstance("db_0", db_host, 3306, "root", "");
            shardDataSourceContainer.addDBInstance(dbInstance_remote);
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
        {

            DBInstance dbInstance_remote = new DBInstance("db_2",db_host, 3306, "root", "");
            shardDataSourceContainer.addDBInstance(dbInstance_remote);
        }
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_0", "db_0", "gsns_test", "gsns"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_1", "db_2", "gsns_test", "gsns"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_2", "db_2", "gsns_test", "gsns_dev"));
        shardDataSourceContainer.addDBShardInstance(new DBShardInstance("gsns_3", "db_2", "gsns_test", "gsns_test"));

        for (int i = 0; i < 3; i++) {
            testConnection(shardDataSourceContainer, "gsns_test", "gsns_0");
            testConnection(shardDataSourceContainer, "gsns_test", "gsns_1");
            testConnection(shardDataSourceContainer, "gsns_test", "gsns_2");
            testConnection(shardDataSourceContainer, "gsns_test", "gsns_3");
        }
    }

    private void testConnection(ShardDataSourceContainer shardDataSourceContainer, String group, String shardId) throws SQLException {
        DataSource dataSource = shardDataSourceContainer.getDataSource(group, shardId);
        Assert.assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        Assert.assertNotNull(connection);
        System.out.println(connection);
        ResultSet resultSet = connection.createStatement().executeQuery("select 1");
        Assert.assertTrue(resultSet.next());
        int anInt = resultSet.getInt(1);
        Assert.assertEquals(1, anInt);
        connection.close();
    }
}
