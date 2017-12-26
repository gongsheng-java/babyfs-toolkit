package com.babyfs.tk.galaxy.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于轮询的负载均衡规则
 */
public class RoundRobinRule implements IRule {

    private AtomicInteger nextIndexAI = new AtomicInteger(0);

    private Logger log = LoggerFactory.getLogger("RoundRobinRule");

    /**
     * 根据传入的ServiceInstance列表，轮询出一个ServiceInstance实例
     *
     * @param list
     * @return
     */
    public ServiceInstance choose(List<ServiceInstance> list) {
        if (list == null) {
            log.warn("no load balancer");
            return null;
        }
        ServiceInstance server = null;
        int index = 0;

        int count = 0;
        while (server == null && count++ < 10) {

            int serverCount = list.size();
            if (serverCount == 0) {
                log.warn("No up servers available from load balancer: " + list);
                return null;
            }
            index = nextIndexAI.incrementAndGet() % serverCount;
            server = list.get(index);
            if (server == null) {
                Thread.yield();
                continue;
            } else {
                return server;
            }
        }
        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + list);
        }
        return server;
    }
}
