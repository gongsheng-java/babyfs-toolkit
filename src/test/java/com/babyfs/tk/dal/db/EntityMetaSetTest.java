package com.babyfs.tk.dal.db;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.meta.ShardGroup;
import com.babyfs.tk.dal.orm.AssignIdEntity;
import com.babyfs.tk.dal.orm.IEntityMeta;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 */
public class EntityMetaSetTest {
    @Entity
    @Table(name="ta")
    @ShardGroup(name = "group0",shardId = "shard0")
    public static class ClassA extends AssignIdEntity {
        private String name;

        @Column(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity
    @Table(name="tb")
    public static class ClassB extends AssignIdEntity {
        private String name;

        @Column(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testAddAndGet() throws Exception {
        EntityMetaSet entityMetaSet = new EntityMetaSet();
        entityMetaSet.add(ClassA.class);
        entityMetaSet.add(ClassB.class);
        Pair<IEntityMeta, IEntityHelper> metaPairA = entityMetaSet.getMetaPair(ClassA.class);
        Pair<IEntityMeta, IEntityHelper> metaPairB = entityMetaSet.getMetaPair(ClassB.class);
        Assert.assertNotNull(metaPairA);
        Assert.assertNotNull(metaPairA.first);
        Assert.assertNotNull(metaPairA.second);
        System.out.println(metaPairA);
        Assert.assertNotNull(metaPairB);
        Assert.assertNotNull(metaPairB.first);
        Assert.assertNotNull(metaPairB.second);
        System.out.println(metaPairB);
    }
}
