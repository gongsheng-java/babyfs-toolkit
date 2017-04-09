package com.babyfs.tk.service.biz.counter;


import com.babyfs.tk.commons.base.Pair;

import java.util.Map;

/**
 * 临时性的计数服务,用户非关键业务
 */
public interface ITemporalCounterService {
    /**
     * 修改计数
     *
     * @param type          计数器的类型,>0
     * @param id            计数器的id,非空
     * @param fieldAndDelta 计数器的field及其增量
     * @return 返回修改后的值
     */
    boolean incr(int type, String id, Pair<String, Long>... fieldAndDelta);

    /**
     * 删除计数器
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     */
    void del(int type, String id);

    /**
     * 删除计数器中的字段
     *
     * @param type   计数的类型,>0
     * @param id     计数器id,非空
     * @param fields 计数器的field
     */
    void delFields(int type, String id, String... fields);

    /**
     * 取得指定id的全部计数
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     * @return 返回计数值
     */
    Map<String, Long> getAll(int type, String id);

    /**
     * 取得指定id指定字段的值
     *
     * @param type   计数器的类型,>0
     * @param id     计数器的id,非空
     * @param fields 计数器的field
     * @return 返回计数值, 如果不存在返回0
     */
    Map<String, Long> getFields(int type, String id, String... fields);
}
