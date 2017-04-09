package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * 实体ES索引服务
 */
public interface IEntityESService<T, A> {
    /**
     * 建立索引
     *
     * @param entity
     * @param attach
     * @return
     */
    ServiceResponse<Void> index(T entity, A attach);

    /**
     * 更新索引
     *
     * @param entity
     * @param attach
     * @return
     */
    ServiceResponse<Void> update(T entity, A attach);

    /**
     * 删除
     *
     * @param id id
     * @return
     */
    ServiceResponse<Void> delete(long id);
}