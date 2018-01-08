package com.babyfs.tk.galaxy.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于轮询的负载均衡规则
 */
public class RoundRobinRule implements IRule {

    private AtomicLong nextIndexAI = new AtomicLong(0);

    private Logger log = LoggerFactory.getLogger("RoundRobinRule");

    private static final int RETRY_TIME = 5;

    /**
     * 根据传入的ServiceInstance列表，轮询出一个ServiceInstance实例
     *
     * @param list
     * @return
     */
    public ServiceInstance choose(List<ServiceInstance> list) {
        if (list == null || list.isEmpty()) {
            log.warn("no load balancer");
            return null;
        }
        ServiceInstance server = null;
        int index = 0;
        int count = 0;
        // 因为list中的可用服务实例可能发生变化,所以此处增加重试机制
        while (server == null && count++ < RETRY_TIME) {
            int serverCount = list.size();
            if (serverCount == 0) {
                log.warn("No up servers available from load balancer: " + list);
                return null;
            }
            index = (int) (nextIndexAI.incrementAndGet() % serverCount);
            server = list.get(index);
            if (server == null) {

                continue;
            } else {
                return server;
            }
        }
        if (count >= RETRY_TIME) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + list);
        }
        return server;
    }
}
