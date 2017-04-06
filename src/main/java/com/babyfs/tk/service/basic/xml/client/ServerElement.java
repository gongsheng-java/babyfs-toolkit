package com.babyfs.tk.service.basic.xml.client;

/**
 */


import com.google.common.collect.Maps;
import com.babyfs.tk.service.basic.xml.common.XmlPropertyElement;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

/**
 * redis服务器配置节点
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "server")
public class ServerElement {

	@XmlAttribute(name="name",required = true)
	private String name = "";
    @XmlJavaTypeAdapter(XmlPropertyElement.MapAdapter.class)
    private Map<String, String> properties = Maps.newHashMap();

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
