package com.babyfs.tk.dal.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 */
@XmlRootElement(name = "dbInstances")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlDBInstances {
    @XmlElement(name = "server")
    private List<XmlServerInstance> instanceXmls;

    public List<XmlServerInstance> getInstances() {
        return instanceXmls;
    }

    public void setInstances(List<XmlServerInstance> instanceXmls) {
        this.instanceXmls = instanceXmls;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("XmlDBInstances");
        sb.append("{instanceXmls=").append(instanceXmls);
        sb.append('}');
        return sb.toString();
    }
}
