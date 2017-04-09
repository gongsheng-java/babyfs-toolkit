package com.babyfs.tk.service.biz.list.dal;


import com.babyfs.tk.dal.db.IListDao;
import com.babyfs.tk.service.biz.base.entity.list.BaseListEntity;

/**
 * Base Dao  For {@link BaseListEntity}
 */
public interface IBaseListDao<T extends BaseListEntity> extends IListDao<T> {


}
