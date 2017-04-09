package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.dal.db.annotation.EntityParam;
import com.babyfs.tk.dal.db.annotation.Sql;
import com.babyfs.tk.dal.db.annotation.SqlParam;
import com.babyfs.tk.dal.db.annotation.SqlType;
import com.babyfs.tk.service.biz.base.entity.IBaseEntity;

/**
 * {@link IBaseEntity} 的基础Dao实现,使用该接口时,有以下假设的前提:
 * <p>
 * {@link IBaseEntity#getVer()}在数据库表中的字段名是`ver`
 * <p>
 * {@link IBaseEntity#getDel()} ()}在数据库表中的字段名是`del`
 */
public interface IBaseDao<T extends IBaseEntity> extends IDao<T> {
    /**
     * 更新实体,检查{@link IBaseEntity#getVer()}版本号是否匹配.
     * 需要注意该方法不更新实体的版本号,如果更新失败,需要将{@link IBaseEntity#getVer()}重置
     *
     * @param entity  entity
     * @param version 期望的版本号,{@link IBaseEntity#getVer()}
     * @return true, 更新成功;false,更新失败
     */
    @Sql(type = SqlType.UPDATE_ENTITY, condition = " ver = :pre_ver")
    boolean updateWithVersion(@EntityParam T entity, @SqlParam("pre_ver") long version);

    /**
     * 设置软删的标志
     *
     * @param id      实体的ID
     * @param del     0=未删除;1=已删除
     * @param version id对应的实体的当前版本号
     */
    @Sql(type = SqlType.UPDATE_COLUMNS, columns = " del = :del,ver = ver + 1", condition = "where id =:id AND ver =:pre_ver")
    int softDelete(@SqlParam("id") long id, @SqlParam("del") byte del, @SqlParam("pre_ver") long version);
}
