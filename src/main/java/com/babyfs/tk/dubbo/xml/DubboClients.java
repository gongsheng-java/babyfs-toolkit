package com.babyfs.tk.dubbo.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "DubboClients")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class DubboClients {

    @XmlElement(name = "client")
    private List<DubboClient> dubboClients;

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class DubboClient{

        @XmlAttribute(required = true)
        private String version;

        @XmlAttribute(required = true)
        private String url;

        @XmlAttribute(required = true)
        private String registry;

        @XmlAttribute(required = true)
        private String type;


        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getRegistry() {
            return registry;
        }

        public void setRegistry(String registry) {
            this.registry = registry;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public List<DubboClient> getDubboClients() {
        return dubboClients;
    }

    public void setDubboClients(List<DubboClient> dubboClients) {
        this.dubboClients = dubboClients;
    }
}
