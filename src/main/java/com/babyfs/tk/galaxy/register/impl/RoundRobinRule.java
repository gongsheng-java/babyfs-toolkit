package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.servicetk.grpcapicore.gray.GrayContext;
import com.babyfs.tk.galaxy.register.IRule;
import com.babyfs.tk.galaxy.register.ServiceServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    @Override
    public ServiceServer choose(List<ServiceServer> list, List<ServiceServer> grayList) {
        List<ServiceServer> chosenList;
        if(GrayContext.get().isHasTag() && grayList != null && grayList.size() > 0){
            chosenList = grayList;
        }else{
            chosenList = list;
        }

        if (chosenList == null || chosenList.isEmpty()) {
            log.warn("no ServiceInstance");
            return null;
        }

        ServiceServer server;
        int index;

        int serverCount = chosenList.size();
        index = (int) (nextIndexAI.incrementAndGet() % serverCount);
        if (index < 0) {
            index = 0;
            nextIndexAI.set(0);
        }
        server = chosenList.get(index);
        return server;
    }

    @Override
    public ServiceServer chooseAfterFilter(List<ServiceServer> list, List<ServiceServer> grayList, Set<ServiceServer> exceptionServer) {
        List<ServiceServer> filtered = list.stream().filter(p -> !exceptionServer.contains(p)).collect(Collectors.toList());
        List<ServiceServer> filteredGray = grayList.stream().filter(p -> !exceptionServer.contains(p)).collect(Collectors.toList());

        if(filtered.size() == 0) {
            return null;
        }
        return choose(filtered, filteredGray);
    }
}
