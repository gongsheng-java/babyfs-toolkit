package com.babyfs.tk.service.biz.kvconf;


import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.biz.base.model.ParsedEntity;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;

import java.util.List;

/**
 * KV配置业务服务
 */
public interface IKVConfService {
    /**
     * 新增
     *
     * @param entity 非空
     * @return 操作结果
     */
    ServiceResponse<KVConfEntity> add(KVConfEntity entity);

    /**
     * 更新
     *
     * @param entity 非空
     * @return 操作结果
     */
    ServiceResponse<Boolean> update(KVConfEntity entity);

    /**
     * 根据id查询,根据类型解析{@link KVConfEntity#getContent()}
     *
     * @param id id
     * @return 操作结果
     */
    ServiceResponse<ParsedEntity<KVConfEntity, Object>> get(long id);

    /**
     * 根据id查询,根据类型解析{@link KVConfEntity#getContent()},优先从本地架子啊
     *
     * @param id
     * @return
     */
    ServiceResponse<ParsedEntity<KVConfEntity, Object>> getWithLocalCache(long id);

    /**
     * 根据id数组获取配置列表
     *
     * @param ids
     * @return
     */
    ServiceResponse<List<ParsedEntity<KVConfEntity, Object>>> getList(long[] ids);

    /**
     * 根据id数组获取配置列表,优先从本地缓存中加载
     *
     * @param ids
     * @return
     */
    ServiceResponse<List<ParsedEntity<KVConfEntity, Object>>> getListWithLocalCache(long[] ids);

    /**
     * 根据名称查询,根据类型解析{@link KVConfEntity#getContent()}
     *
     * @param name 非空
     * @return
     */
    ServiceResponse<ParsedEntity<KVConfEntity, Object>> getByName(String name);

    /**
     * 根据名称查询,优先从本地Cache中加载,本地如果没有数据,再从{@link #getByName(String)}加载.
     * 根据类型解析{@link KVConfEntity#getContent()}
     *
     * @param name
     * @return
     */
    ServiceResponse<ParsedEntity<KVConfEntity, Object>> getByNameWithLocalCache(String name);

    /**
     * 删除一个字典
     *
     * @param id id
     * @return 操作结果
     */
    ServiceResponse<Boolean> del(long id);

    /**
     * 是否是系统内置的配置
     *
     * @param name not null
     * @return
     */
    boolean isSysName(String name);

    /**
     * 根据类型{@link KVConfEntity#getContent()},将值设置到{@link ParsedEntity#parsed}
     *
     * @param entity
     */
    ParsedEntity<KVConfEntity, Object> parse(KVConfEntity entity);
}
