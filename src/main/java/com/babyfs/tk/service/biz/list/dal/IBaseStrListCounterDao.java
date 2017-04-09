package com.babyfs.tk.service.biz.list.dal;


import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.base.entity.list.BaseStrListCounterEntity;

import java.util.List;

/**
 * Base Dao  For {@link BaseStrListCounterEntity}
 */
public interface IBaseStrListCounterDao<T extends BaseStrListCounterEntity> extends IDao<T> {
    /**
     * 插入或者更新计数值,如果计数器不存在,则插入纪录(仅支持MySQL)
     *
     * @param ownerId 属主的ID
     * @param value   如果id对应的数据不存在,插入数据的初始值
     * @return 操作结果
     */
    @Sql(type = SqlType.EXEC, condition = "", execSql = "INSERT INTO %s (o_id,counter) VALUES (:o_id,:value) ON DUPLICATE KEY UPDATE counter=:value")
    int insertOrUpdate(@SqlParam("o_id") String ownerId, @SqlParam("value") long value);

    /**
     * 根据owner Id查询计数
     *
     * @param ownerId 属主的ID
     * @return 操作结果
     */
    @Sql(type = SqlType.QUERY_COLUMNS, columns = "counter", condition = "where o_id =:o_id")
    List<Object[]> getByOwnerId(@SqlParam("o_id") String ownerId);

    /**
     * 根据owner Id删除计数
     *
     * @param ownerId 属主的ID
     * @return 操作结果
     */
    @Sql(type = SqlType.DELETE, columns = " ", condition = "where o_id =:o_id")
    int delByOwnerId(@SqlParam("o_id") String ownerId);
}
