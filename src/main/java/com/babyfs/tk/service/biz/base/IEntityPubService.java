package com.babyfs.tk.service.biz.base;

import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * 实体事件变更通知服务
 */
public interface IEntityPubService<T, A> {
    /**
     * 建立索引
     *
     * @param entity
     * @param attach
     * @return
     */
    ServiceResponse<Void> add(T entity, A attach);

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
     * @param entity entity
     * @return
     */
    ServiceResponse<Void> delete(T entity);
}