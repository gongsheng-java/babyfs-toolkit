package com.babyfs.tk.service.biz.kvconf;


import com.babyfs.tk.service.biz.base.IDataService;
import com.babyfs.tk.service.biz.base.query.PageParams;
import com.babyfs.tk.service.biz.base.query.PageResult;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
import com.babyfs.tk.service.biz.kvconf.model.KVConfQuery;

/**
 * {@link KVConfEntity}的数据服务接口
 */
public interface IKVConfDataService extends IDataService<KVConfEntity> {

    /**
     * 根据名称查询
     *
     * @param name 名称
     * @return
     */
    KVConfEntity getByName(String name);

    /**
     * KV查询
     *
     * @param pageParams 页参数
     * @param queryParam 查询参数
     * @return 查询结果
     */
    PageResult<KVConfEntity> query(PageParams pageParams, KVConfQuery queryParam);
}
