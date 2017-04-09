package com.babyfs.tk.dal.db;

import com.babyfs.tk.dal.orm.IEntity;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Dao的基础接口
 */
public interface IDao<T extends IEntity> {
    /**
     * 保存一个实体对象
     *
     * @param entity
     * @return
     */
    T save(@Nonnull T entity);

    /**
     * 保存一个实体
     *
     * @param entity
     * @return
     */
    boolean update(@Nonnull IEntity entity);

    /**
     * 删除一个实体
     *
     * @param entity
     * @return
     */
    boolean delete(@Nonnull IEntity entity);

    /**
     * 取得一个实体
     *
     * @param id
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends IEntity> T get(@Nonnegative long id, Class<T> clazz);
}
