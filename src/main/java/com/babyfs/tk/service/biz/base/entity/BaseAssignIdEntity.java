package com.babyfs.tk.service.biz.base.entity;

import com.babyfs.tk.dal.orm.AssignIdEntity;

import javax.persistence.Column;

/**
 *
 */
public abstract class BaseAssignIdEntity extends AssignIdEntity implements IBaseEntity {
    private static final long serialVersionUID = -9197761427369601143L;
    /**
     * 创建时间
     */
    private long ct;
    /**
     * 最后更新时间
     */
    private long ut;
    /**
     * 版本号, 防止脏读写
     */
    private long ver;
    /**
     * 软删标识 0=未删除;1=已删除
     */
    private byte del;

    @Column(name = "ct")
    public long getCt() {
        return this.ct;
    }

    public void setCt(long val) {
        this.ct = val;
    }

    @Column(name = "ut")
    public long getUt() {
        return this.ut;
    }

    public void setUt(long val) {
        this.ut = val;
    }

    @Column(name = "ver")
    public long getVer() {
        return this.ver;
    }

    public void setVer(long ver) {
        this.ver = ver;
    }

    @Column(name = "del")
    public byte getDel() {
        return this.del;
    }

    public void setDel(byte val) {
        this.del = val;
    }
}
