package com.babyfs.tk.galaxy.register;

import javax.xml.ws.Service;
import java.util.List;
import java.util.Set;

/**
 * 负载均衡器规则接口
 */
public interface IRule {
    /**
     * 从传入的服务实例列表中根据规则选择出一个ServiceInstance
     *
     * @param list
     * @return 可用服务实例
     */
    ServiceServer choose(List<ServiceServer> list);

    ServiceServer chooseAfterFilter(List<ServiceServer> list, Set<ServiceServer> exceptionServerSet);
}
