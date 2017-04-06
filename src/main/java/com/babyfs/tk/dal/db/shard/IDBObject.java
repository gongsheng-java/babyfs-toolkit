package com.babyfs.tk.dal.db.shard;

/**
 * 数据库的定义对象接口
 */
public interface IDBObject {

    /**
     * 取得该对象的id,对于某一类对象id必须的全局惟一的
     *
     * @return
     */
    public String getId();

    /**
     * 取得该对象的序列Id.
     * 序列id用于在JVM中惟一标识一个数据库定义的对象,当相同id的对象({@link #getId()})变化时,可以通过这个id获取变化的信息
     *
     * @return
     */
    public int getSeqId();
}
