package com.babyfs.tk.service.biz.list.dal;


import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.base.entity.list.BaseListCounterEntity;

/**
 * Base Dao  For {@link BaseListCounterEntity}
 */
public interface IBaseListCounterDao<T extends BaseListCounterEntity> extends IDao<T> {
    /**
     * 增加或减少计数器,如果计数器不存在,则插入纪录(仅支持MySQL)
     *
     * @param id        属主的ID
     * @param initValue 如果id对应的数据不存在,插入数据的初始值
     * @param delta     正数表示增加;负数表示减少
     * @return 操作结果
     */
    @Sql(type = SqlType.EXEC, condition = "", execSql = "INSERT INTO %s (id,counter) VALUES (:id,:init_value) ON DUPLICATE KEY UPDATE counter=counter+:delta")
    int incrAndInsertIfAbsent(@SqlParam("id") long id, @SqlParam("init_value") long initValue, @SqlParam("delta") int delta);

    /**
     * 增加或减少计数器(仅支持MySQL)
     *
     * @param id    属主的ID
     * @param delta 正数表示增加;负数表示减少
     * @return 操作结果
     */
    @Sql(type = SqlType.UPDATE_COLUMNS, columns = " counter = counter + :delta", condition = "where id =:id")
    int incr(@SqlParam("id") long id, @SqlParam("delta") int delta);

    /**
     * 插入或者更新计数值,如果计数器不存在,则插入纪录(仅支持MySQL)
     *
     * @param id    属主的ID
     * @param value 计数值
     * @return 操作结果
     */
    @Sql(type = SqlType.EXEC, condition = "", execSql = "INSERT INTO %s (id,counter) VALUES (:id,:value) ON DUPLICATE KEY UPDATE counter=:value")
    int insertOrUpdate(@SqlParam("id") long id, @SqlParam("value") long value);
}
