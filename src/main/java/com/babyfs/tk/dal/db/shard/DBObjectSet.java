package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.*;

/**
 * 数据库实例的集合
 */
public class DBObjectSet<T extends IDBObject> {
    private volatile ConcurrentMap<String, T> instances = Maps.newConcurrentMap();
    private volatile Map<String, T> unmodifiedMap = Collections.unmodifiableMap(instances);

    /**
     * 增加一个实例
     *
     * @param instance 数据库实例
     * @throws IllegalStateException 当新增加的数据库实例id重复时会抛出此异常
     */
    public synchronized void add(@Nonnull T instance) {
        checkNotNull(instance, "instance");
        T preInstance = instances.putIfAbsent(instance.getId(), instance);
        checkState(preInstance == null, "Duplicate instance id,new:%s,old:%s", instance, preInstance);
    }

    /**
     * 删除指定id的实例
     *
     * @param instanceId
     * @return
     */
    public synchronized T remove(@Nonnull String instanceId) {
        checkArgument(!Strings.isNullOrEmpty(instanceId), "instanceId");
        return instances.remove(instanceId);
    }

    /**
     * 取得指定id的实例
     *
     * @param instanceId
     * @return
     */
    public T get(@Nonnull String instanceId) {
        checkArgument(!Strings.isNullOrEmpty(instanceId), "instanceId");
        return instances.get(instanceId);
    }

    /**
     * @return
     */
    public Map<String, T> getAll() {
        return unmodifiedMap;
    }

    /**
     * 重新加载
     *
     * @param instances
     */
    public synchronized void reload(Iterable<T> instances) {
        checkNotNull(instances, "instances");
        ConcurrentMap<String, T> newInstances = Maps.newConcurrentMap();
        int count = 0;
        for (T instance : instances) {
            T preInstance = newInstances.putIfAbsent(instance.getId(), instance);
            checkState(preInstance == null, "Duplicate instance,new:%s,old:%s", instance, preInstance);
            count++;
        }
        checkState(count > 0, "Can't reload empty instances");
        this.instances = newInstances;
        this.unmodifiedMap = Collections.unmodifiableMap(this.instances);
    }
}
