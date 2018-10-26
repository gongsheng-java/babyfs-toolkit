package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.commons.model.Operator;
import com.babyfs.tk.service.biz.base.entity.BaseOperatorEntity;
import com.babyfs.tk.service.biz.base.entity.IBaseEntity;
import com.google.common.base.Strings;

/**
 * @program BabyFs
 * @description: 附带operator的db操作
 * @author: huyihuan
 * @create: 2018/10/23
 */
public interface IOperatorDataService<T extends BaseOperatorEntity> extends IDataService<T>  {

    T add(T entity, Operator operator);

    boolean update(T entity, Operator operator);

    boolean softDel(T entity, Operator operator);

    boolean cancelSoftDel(T entity, Operator operator);

    boolean updateWithVersion(T entity, Operator operator);
}
