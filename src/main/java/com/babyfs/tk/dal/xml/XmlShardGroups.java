package com.babyfs.tk.dal.xml;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

/**
 */
@XmlRootElement(name = "shardGroups")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlShardGroups {

    @XmlElement(name = "defaultShard")
    private DefaultGroup defaultShard;

    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, XmlShardGroup> groups;

    public DefaultGroup getDefaultShard() {
        return defaultShard;
    }

    public void setDefaultShard(DefaultGroup defaultShard) {
        this.defaultShard = defaultShard;
    }

    public Map<String, XmlShardGroup> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, XmlShardGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("XmlShardGroups");
        sb.append("{groups=").append(groups);
        sb.append('}');
        return sb.toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DefaultGroup {
        @XmlAttribute(name = "groupId")
        private String groupId;
        @XmlAttribute(name = "shardId")
        private String shardId;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getShardId() {
            return shardId;
        }

        public void setShardId(String shardId) {
            this.shardId = shardId;
        }
    }

    public static class ShardGroupType {
        private List<XmlShardGroup> xmlShardGroups;

        @XmlElement(name = "shardGroup")
        public List<XmlShardGroup> getShardGroups() {
            return xmlShardGroups;
        }

        public void setShardGroups(List<XmlShardGroup> xmlShardGroups) {
            this.xmlShardGroups = xmlShardGroups;
        }
    }

    public static final class MapAdapter extends XmlAdapter<ShardGroupType, Map<String, XmlShardGroup>> {
        public MapAdapter() {
        }

        @Override
        public Map<String, XmlShardGroup> unmarshal(ShardGroupType v) throws Exception {
            Map<String, XmlShardGroup> shardGroupMap = Maps.newHashMap();
            for (XmlShardGroup xmlShardGroup : v.getShardGroups()) {
                Preconditions.checkArgument(!shardGroupMap.containsKey(xmlShardGroup.getId()), "Duplicate shard group id %s", xmlShardGroup.getId());
                shardGroupMap.put(xmlShardGroup.getId(), xmlShardGroup);
            }
            return shardGroupMap;
        }

        @Override
        public ShardGroupType marshal(Map<String, XmlShardGroup> v) throws Exception {
            List<XmlShardGroup> xmlShardGroups = Lists.newArrayList();
            xmlShardGroups.addAll(v.values());
            ShardGroupType shardGroupType = new ShardGroupType();
            shardGroupType.setShardGroups(xmlShardGroups);
            return shardGroupType;
        }
    }
}
