package com.babyfs.tk.service.biz.service.backend.user.model.bean;

/**
 * 资源
 */
public interface Resource {
    /**
     * 取得资源的名称
     *
     * @return
     */
    String getName();

    /**
     * 取得资源的ID
     *
     * @return
     */
    String getId();

    /**
     * 取得资源的类型
     *
     * @return
     */
    int getType();
}
