package com.babyfs.tk.galaxy.register;

import java.util.List;

/**
 * 负载均衡器规则接口
 */
public interface IRule {
    /**
     * 从传入的服务实例列表中根据规则选择出一个ServiceInstance
     * 此list是线程安全的List
     * @param list
     * @return 可用服务实例
     */
    ServiceInstance choose(List<ServiceInstance> list);
}
