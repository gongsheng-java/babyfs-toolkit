package com.babyfs.tk.galaxy.register;

import java.util.List;

/**
 * 负载均衡器规则接口
 */
public interface IRule {

    ServiceInstance choose(List<ServiceInstance> list);
}
