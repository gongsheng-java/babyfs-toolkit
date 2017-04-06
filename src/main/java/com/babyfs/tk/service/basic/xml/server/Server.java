package com.babyfs.tk.service.basic.xml.server;

/**
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File Templates.
 */


import com.google.common.collect.Maps;
import com.babyfs.tk.service.basic.xml.common.XmlPropertyElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

/**
 * redis服务器配置节点
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "host")
public class Server {
	@XmlAttribute(name="name", required = true)
	private String name;
    @XmlAttribute(name="port", required = true)
	private String port;
    @XmlAttribute(name="host", required = true)
	private String host;
    @XmlAttribute(name = "password", required = false)
    private String password="";

    @XmlJavaTypeAdapter(XmlPropertyElement.MapAdapter.class)
    private Map<String, String> properties = Maps.newHashMap();

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
