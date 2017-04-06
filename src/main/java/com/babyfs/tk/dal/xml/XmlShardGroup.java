package com.babyfs.tk.dal.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlShardGroup {
    @XmlAttribute(required = true, name = "id")
    private String id;
    @XmlElement(name = "shardInstance", required = true)
    private List<XmlShardInstance> xmlShardInstances;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<XmlShardInstance> getShardInstances() {
        return xmlShardInstances;
    }

    public void setShardInstances(List<XmlShardInstance> xmlShardInstances) {
        this.xmlShardInstances = xmlShardInstances;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("XmlShardGroup");
        sb.append("{id='").append(id).append('\'');
        sb.append(", xmlShardInstances=").append(xmlShardInstances);
        sb.append('}');
        return sb.toString();
    }
}
