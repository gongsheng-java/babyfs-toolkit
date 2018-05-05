package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.ServiceServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于轮询的负载均衡规则
 */
public class RoundRobinRule implements IRule {
    private Logger log = LoggerFactory.getLogger(RoundRobinRule.class);

    private final AtomicLong nextIndexAI = new AtomicLong(0);

    /**
     * 根据传入的ServiceInstance列表，轮询出一个ServiceInstance实例
     *
     * @param list
     * @return
     */
    public ServiceServer choose(List<ServiceServer> list) {
        if (list == null || list.isEmpty()) {
            log.warn("no ServiceInstance");
            return null;
        }

        ServiceServer server;
        int index;

        int serverCount = list.size();
        index = (int) (nextIndexAI.incrementAndGet() % serverCount);
        if (index < 0) {
            index = 0;
            nextIndexAI.set(0);
        }
        server = list.get(index);
        return server;
    }
}
