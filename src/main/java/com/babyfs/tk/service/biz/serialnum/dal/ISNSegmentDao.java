package com.babyfs.tk.service.biz.serialnum.dal;


import com.babyfs.tk.dal.db.annotation.Dao;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.base.IBaseDao;
import com.babyfs.tk.service.biz.serialnum.model.SNSegmentEntity;

import java.util.List;

/**
 * Dao for {@link SNSegmentEntity}
 */
@Dao(entityClass = SNSegmentEntity.class)
public interface ISNSegmentDao extends IBaseDao<SNSegmentEntity> {

    /**
     * 根据type查询
     *
     * @param type
     * @return
     */
    @Sql(type = SqlType.QUERY_ENTITY, condition = "WHERE type = :type LIMIT 1")
    List<SNSegmentEntity> queryOneByType(@SqlParam("type") int type);
}
