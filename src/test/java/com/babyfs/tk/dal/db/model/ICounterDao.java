package com.babyfs.tk.dal.db.model;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;

/**
 */
@Dao(entityClass = Counter.class)
public interface ICounterDao extends IDao<Counter> {

    /**
     * @param ownerId
     * @param delta
     * @return
     */
    @Sql(type = SqlType.EXEC, condition = "", execSql = "INSERT INTO %s (id,counter) VALUES (:id,:init_value) ON DUPLICATE KEY UPDATE counter=counter+:delta;")
    int incr(@SqlParam("id") long ownerId, @SqlParam("init_value") long initValue, @SqlParam("delta") int delta);
}
