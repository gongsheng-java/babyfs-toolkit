package com.babyfs.tk.galaxy.register;

import java.util.List;

/**
 * 负载均衡器规则接口
 */
public interface IRule {
    /**
     * 从传入的服务实例列表中根据规则选择出一个ServiceInstance
     * @param list
     * @return
     */
    ServiceInstance choose(List<ServiceInstance> list);
}
