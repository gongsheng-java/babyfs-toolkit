package com.babyfs.tk.dal.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 */
@XmlRootElement(name = "entityShards")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlEntityShards {

    @XmlElement(name = "entityShard")
    private List<XmlEntityShard> entityShards;

    public List<XmlEntityShard> getEntityShards() {
        return entityShards;
    }

    public void setEntityShards(List<XmlEntityShard> entityShards) {
        this.entityShards = entityShards;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlEntityShard {
        @XmlAttribute(required = true)
        private String className;
        @XmlAttribute(required = true)
        private String groups;
        @XmlElement(name = "dbShardStrategies")
        private ShardStrategyType dbShardStrategies;
        @XmlElement(name = "tableShardStrategies")
        private ShardStrategyType tableShardStrategies;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getGroups() {
            return groups;
        }

        public void setGroups(String groups) {
            this.groups = groups;
        }

        public ShardStrategyType getDbShardStrategies() {
            return dbShardStrategies;
        }

        public void setDbShardStrategies(ShardStrategyType dbShardStrategies) {
            this.dbShardStrategies = dbShardStrategies;
        }

        public ShardStrategyType getTableShardStrategies() {
            return tableShardStrategies;
        }

        public void setTableShardStrategies(ShardStrategyType tableShardStrategies) {
            this.tableShardStrategies = tableShardStrategies;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ShardStrategyType {
        @XmlElement(name = "shardStrategy")
        private List<XmlShardStrategy> xmlShardStrategies;

        public List<XmlShardStrategy> getShardStrategies() {
            return xmlShardStrategies;
        }

        public void setShardStrategies(List<XmlShardStrategy> xmlShardStrategies) {
            this.xmlShardStrategies = xmlShardStrategies;
        }
    }
}
