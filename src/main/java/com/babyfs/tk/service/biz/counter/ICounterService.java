package com.babyfs.tk.service.biz.counter;


import com.babyfs.tk.commons.base.Pair;

import java.util.List;
import java.util.Map;

/**
 * 计数器
 */
public interface ICounterService {
    /**
     * 修改计数
     *
     * @param type          计数器的类型,>0
     * @param id            计数器的id,非空
     * @param fieldAndDelta field及其增量
     * @return 返回修改后的值
     */
    boolean incr(int type, String id, List<Pair<String, Long>> fieldAndDelta);

    /**
     * 删除计数
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     */
    void del(int type, String id);

    /**
     * 取得计数
     *
     * @param type 计数器的类型,>0
     * @param id   计数器的id,非空
     * @return 返回计数值, 如果不存在返回0
     */
    Map<String, Long> get(int type, String id);

}
