package com.babyfs.tk.galaxy.register.impl;

import com.babyfs.tk.commons.enums.ShutdownOrder;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.galaxy.register.IServcieRegister;
import com.google.common.collect.Sets;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Set;

/**
 * 本地的服务注册,其实没做注册
 */
@Order
@ShutdownOrder
public class LocalServiceRegister extends LifeServiceSupport implements IServcieRegister {
    /**
     * 服务接口
     */
    private Set<String> interfaceNames = Sets.newConcurrentHashSet();

    /**
     */
    public LocalServiceRegister() {
    }

    @Override
    public synchronized void addServices(List<String> serviceNames) {
        if (ListUtil.isEmpty(serviceNames)) {
            return;
        }
        this.interfaceNames.addAll(serviceNames);
    }

    @Override
    public synchronized void removeServices(List<String> serviceNames) {
        if (ListUtil.isEmpty(serviceNames)) {
            return;
        }
        this.interfaceNames.removeAll(serviceNames);
    }

    @Override
    public synchronized void updateRegister() {
    }

    @Override
    protected synchronized void execStart() {
        super.execStart();
    }

    @Override
    protected synchronized void execStop() {
        super.execStop();
    }

}
