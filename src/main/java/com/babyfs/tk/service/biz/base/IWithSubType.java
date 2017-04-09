package com.babyfs.tk.service.biz.base;


import com.babyfs.tk.service.biz.base.model.ISubType;

import java.util.List;

/**
 * 带有子类型的接口
 */
public interface IWithSubType<T extends ISubType> {
    /**
     * 取得所有的子类型
     *
     * @return
     */
    List<T> getSubTypes();

    /**
     * 根据索引查找子类型
     *
     * @param index
     * @return
     */
    T indexOfSubType(int index);
}
