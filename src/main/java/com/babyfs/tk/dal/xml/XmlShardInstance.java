package com.babyfs.tk.dal.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * shard的实例
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlShardInstance {
    @XmlAttribute(required = true)
    private String id;
    @XmlAttribute(required = true)
    private String dbInstanceId;
    @XmlAttribute(required = true)
    private String schema;
    @XmlElement(name = "parameter")
    private List<XmlParameter> parameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(String dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<XmlParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<XmlParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("XmlShardInstance");
        sb.append("{id='").append(id).append('\'');
        sb.append(", dbInstanceId='").append(dbInstanceId).append('\'');
        sb.append(", schema='").append(schema).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
