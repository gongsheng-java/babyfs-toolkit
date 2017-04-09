package com.babyfs.tk.dal.orm;

import java.io.Serializable;

/**
 * 所有实体对象的基类,每个实体类都有一个id属性(数据库字段).
 * 所有的数据库属性都使用<code>javax.persistence</code>定义的Annotation进行标记
 * <p/>
 */
public interface IEntity extends Serializable {
    long getId();

    void setId(long id);
}
