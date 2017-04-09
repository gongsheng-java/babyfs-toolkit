package com.babyfs.tk.dal.db;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.orm.IEntityMeta;
import com.babyfs.tk.dal.meta.SimpleEntityMeta;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;

/**
 */
public class EntityMetaSet {
    private final ConcurrentMap<Class, Pair<IEntityMeta, IEntityHelper>> entityMetaMap = Maps.newConcurrentMap();

    /**
     * 注册实体类型
     *
     * @param entityClass
     * @param <T>         实体的类型
     * @throws IllegalArgumentException
     */
    public <T extends IEntity> void add(@Nonnull Class<T> entityClass) {
        Preconditions.checkNotNull(entityClass, "entityClass");
        IEntityMeta simpleEntityMeta = new SimpleEntityMeta<T>(entityClass);
        IEntityHelper entityHelper = EntityHelperGenerator.generateConverterInstance(simpleEntityMeta);
        Pair<IEntityMeta, IEntityHelper> pair = new Pair<IEntityMeta, IEntityHelper>(simpleEntityMeta, entityHelper);
        Pair<IEntityMeta, IEntityHelper> pre = entityMetaMap.putIfAbsent(entityClass, pair);
        Preconditions.checkState(pre == null, "Duplicate register for Entity class %s", entityClass);
    }

    /**
     * 取得实体的类型元信息
     *
     * @param entityClass
     * @return
     */
    public Pair<IEntityMeta, IEntityHelper> getMetaPair(Class entityClass) {
        return this.entityMetaMap.get(entityClass);
    }
}
