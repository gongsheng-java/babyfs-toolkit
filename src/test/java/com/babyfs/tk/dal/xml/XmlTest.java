package com.babyfs.tk.dal.xml;

import com.babyfs.tk.commons.xml.JAXBUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 */
public class XmlTest {

    @Test
    public void test_dbinstance_unmarshel() {
        XmlDBInstances unmarshal = JAXBUtil.unmarshal(XmlDBInstances.class, "db_instance.xml");
        Assert.assertNotNull(unmarshal);
        Assert.assertNotNull(unmarshal.getInstances());
        Assert.assertEquals(2, unmarshal.getInstances().size());
        System.out.println(unmarshal);
    }

    @Test
    public void test_shards_unmarshel() {
        XmlShardGroups unmarshal = JAXBUtil.unmarshal(XmlShardGroups.class, "shard_instance.xml");
        Assert.assertNotNull(unmarshal);
        System.out.println(unmarshal);
        XmlShardGroups.DefaultGroup defaultGroup = unmarshal.getDefaultShard();
        Assert.assertNotNull(defaultGroup);
        Assert.assertEquals("default", defaultGroup.getGroupId());
        Assert.assertEquals("default_0",defaultGroup.getShardId());
        JAXBUtil.marshal(unmarshal, System.out);
    }

    @Test
    public void test_entityShard_unmarshal() {
        XmlEntityShards unmarshal = JAXBUtil.unmarshal(XmlEntityShards.class, "entity_shards.xml");
        Assert.assertNotNull(unmarshal);
        XmlEntityShards.XmlEntityShard entityShard = unmarshal.getEntityShards().get(0);
        XmlEntityShards.ShardStrategyType dbShardStrategies = entityShard.getDbShardStrategies();
        List<XmlShardStrategy> xmlShardStrategies = dbShardStrategies.getShardStrategies();
        XmlShardStrategy xmlShardStrategy = xmlShardStrategies.get(0);
        Assert.assertEquals("hash", xmlShardStrategy.getType());
        Assert.assertEquals("3", xmlShardStrategy.getProperties().get("shardCount"));
        System.out.println(unmarshal);

        JAXBUtil.marshal(unmarshal, System.out);
    }
}
