package com.babyfs.tk.service.biz.counter;

import java.util.Map;

/**
 * 计数器持久化服务
 */
public interface ICounterSyncService {
    /**
     * 从持久层取得的计数值
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     * @return 返回字段及其对应的值, 如果type, id对应的值不存在, 也应该返回一个不为空的Map,值为0
     */
    Map<String, Long> get(int type, String id);

    /**
     * 从持久层删除计数器
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     * @return true, 删除成功;false,删除失败
     */
    boolean del(int type, String id);

    /**
     * 将计数值同步到持久层
     *
     * @param type          计数器的类型,>0
     * @param id            计数器的id,非空
     * @param fieldAndValue 字段及其对应的值,非空
     * @return
     */
    boolean sync(int type, String id, Map<String, Long> fieldAndValue);
}
