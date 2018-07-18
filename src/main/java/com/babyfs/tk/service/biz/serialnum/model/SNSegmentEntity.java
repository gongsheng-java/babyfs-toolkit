package com.babyfs.tk.service.biz.serialnum.model;

import com.babyfs.tk.service.biz.base.entity.BaseAutoIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 流水号分段表
 */
@Entity
@Table(name = "t_sn_segment")
public class SNSegmentEntity extends BaseAutoIdEntity {
    /**
     * 流水号类型
     */
    private int type;

    /**
     * 流水号当前最大id
     */
    private long maxId;

    /**
     * 流水号递增或者递减长度
     */
    private int step;

    /**
     * 是否递减(0 不是，1 是)
     */
    private int desc;

    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Column(name = "max_id")
    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    @Column(name = "step")
    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Column(name = "desc")
    public int getDesc() {
        return desc;
    }

    public void setDesc(int desc) {
        this.desc = desc;
    }
}
