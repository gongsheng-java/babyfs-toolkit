package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.commons.model.Operator;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.base.entity.BaseOperatorEntity;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.google.common.base.Strings;

/**
 * @program BabyFs
 * @description: ${TODO}
 * @author: huyihuan
 * @create: 2018/10/23
 */
public class OperatorDataServiceImpl<T extends BaseOperatorEntity> extends BaseDataServiceImpl<T> implements IOperatorDataService<T> {

    /**
     * @param clazz      实体的类型,非空
     * @param dao        dao
     * @param daoFactory
     * @param cacheParam cache parameter
     */
    protected OperatorDataServiceImpl(Class<T> clazz, IBaseDao<T> dao, DaoFactory daoFactory, CacheParameter cacheParam) {
        super(clazz, dao, daoFactory, cacheParam);
    }

    @Override
    public T add(T entity, Operator operator) {
        entity.appendCtOperator(operator);
        return this.add(entity);
    }

    @Override
    public boolean update(T entity, Operator operator) {
        entity.appendUtOperator(operator);
        return this.update(entity);
    }

    @Override
    public boolean softDel(T entity, Operator operator) {
        entity.appendUtOperator(operator);
        return this.softDel(entity);
    }

    @Override
    public boolean cancelSoftDel(T entity, Operator operator) {
        entity.appendUtOperator(operator);
        return this.cancelSoftDel(entity);
    }

    @Override
    public boolean updateWithVersion(T entity, Operator operator) {
        entity.appendUtOperator(operator);
        return this.updateWithVersion(entity);
    }
}
