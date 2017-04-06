package com.babyfs.tk.service.basic.xml.mongodb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * mongoDB的服务结点的配置
 * <p/>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "root")
public class MongodbConfigs {
    @XmlElement(name = "mongodb", required = true)
    private MongodbElement mongodb = null;

    public Map<String, ServiceElement> getServices() {
        Map<String, ServiceElement> elementMap = Maps.newLinkedHashMap();
        if (this.mongodb != null) {
            List<ServiceElement> elements = this.mongodb.getServices();
            for (ServiceElement element : elements) {
                elementMap.put(element.getName(), element);
            }
        }
        return elementMap;
    }

    public Map<String, ClusterElement> getClusters() {
        Map<String, ClusterElement> elementMap = Maps.newLinkedHashMap();
        if (this.mongodb != null) {
            List<ClusterElement> elements = this.mongodb.getClusters();
            for (ClusterElement element : elements) {
                elementMap.put(element.getName(), element);
            }
        }
        return elementMap;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "mongodb")
    public static class MongodbElement {
        @XmlElementWrapper(name = "clusters", required = true)
        @XmlElement(name = "cluster", required = true)
        private List<ClusterElement> clusters = Lists.newArrayList();
        @XmlElementWrapper(name = "services", required = true)
        @XmlElement(name = "service", required = true)
        private List<ServiceElement> services = Lists.newArrayList();

        public List<ClusterElement> getClusters() {
            return clusters;
        }

        public List<ServiceElement> getServices() {
            return services;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "clusters")
    public static class ClusterElement {
        @XmlAttribute(name = "name", required = true)
        private String name;
        @XmlElement(name = "writeConcern", required = false)
        private String writeConcern = "NORMAL";
        @XmlElementWrapper(name = "servers", required = true)
        @XmlElement(name = "server", required = true)
        private List<ServerElement> servers = Lists.newArrayList();
        @XmlElementWrapper(name = "configs")
        @XmlElement(name = "entry")
        private List<ConfigEntryElement> configs = Lists.newArrayList();

        public Map<String, String> getConfigs() {
            return MongodbConfigs.getConfigs(this.configs);
        }

        public List<ServerElement> getServers() {
            return servers;
        }

        public String getWriteConcern() {
            return writeConcern;
        }

        public String getName() {
            return name;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "servers")
    public static class ServerElement {
        @XmlAttribute(name = "host", required = true)
        private String host;
        @XmlAttribute(name = "port", required = true)
        private String port;

        public String getPort() {
            return port;
        }

        public String getHost() {
            return host;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "servers")
    public static class ServiceElement {
        @XmlAttribute(name = "name", required = true)
        private String name;
        @XmlElement(name = "cluster", required = true)
        private String cluster;
        @XmlElement(name = "dbName", required = true)
        private String dbName;
        @XmlElement(name = "collectionName", required = true)
        private String collectionName;
        @XmlElement(name = "writeConcern", required = false)
        private String writeConcern = "NORMAL";
        @XmlElementWrapper(name = "configs")
        @XmlElement(name = "entry")
        private List<ConfigEntryElement> configs = Lists.newArrayList();

        public Map<String, String> getConfigs() {
            return MongodbConfigs.getConfigs(this.configs);
        }

        public String getWriteConcern() {
            return writeConcern;
        }

        public String getCluster() {
            return cluster;
        }

        public String getName() {
            return name;
        }

        public String getDbName() {
            return dbName;
        }

        public String getCollectionName() {
            return collectionName;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "configs")
    public static class ConfigEntryElement {
        @XmlAttribute(name = "name", required = true)
        private String name;
        @XmlValue
        private String value;
        @XmlAttribute(name = "value", required = false)
        private String valueAttr;

        public String getName() {
            return name;
        }

        public String getValue() {
            if (valueAttr != null) {
                return valueAttr;
            }
            return value;
        }
    }

    private static final Map<String, String> getConfigs(final List<ConfigEntryElement> configs) {
        Map<String, String> elementMap = Maps.newLinkedHashMap();
        if (configs != null) {
            for (ConfigEntryElement element : configs) {
                elementMap.put(element.getName(), element.getValue());
            }
        }
        return elementMap;
    }
}
