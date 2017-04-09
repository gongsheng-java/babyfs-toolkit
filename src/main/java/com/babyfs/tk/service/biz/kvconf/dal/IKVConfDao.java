package com.babyfs.tk.service.biz.kvconf.dal;

import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.base.IBaseDao;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;

import java.util.List;

/**
 * Dao for {@link KVConfEntity}
 */
@Dao(entityClass = KVConfEntity.class)
public interface IKVConfDao extends IBaseDao<KVConfEntity> {

    /**
     * 根据name查询
     *
     * @param name
     * @return
     */
    @Sql(type = SqlType.QUERY_ENTITY, condition = "WHERE name = :name LIMIT 1")
    List<KVConfEntity> queryOneByName(@SqlParam("name") String name);
}
