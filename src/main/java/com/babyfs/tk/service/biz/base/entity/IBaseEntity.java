package com.babyfs.tk.service.biz.base.entity;

import com.babyfs.tk.dal.orm.IEntity;

/**
 * 带有版本号,创建和更新时间,以及删除标志的实体
 */
public interface IBaseEntity extends IEntity {
    /**
     * 返回创建时间
     *
     * @return
     */
    long getCt();

    /**
     * 设置创建时间
     *
     * @param val
     */
    void setCt(long val);

    /**
     * 返回更新时间
     *
     * @return
     */
    long getUt();

    /**
     * 设置更新时间
     *
     * @param val
     */
    void setUt(long val);

    /**
     * 返回版本号
     *
     * @return
     */
    long getVer();

    /**
     * 设置版本号,
     *
     * @param ver
     */
    void setVer(long ver);

    /**
     * 返回删除标记
     *
     * @return 0=未删除;1=已删除
     */
    byte getDel();

    /**
     * 设置删除标记
     *
     * @param val 0=未删除;1=已删除
     */
    void setDel(byte val);
}
