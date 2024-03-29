package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.enums.ShutdownOrder;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.LifeServiceSupport;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 *
 */
public class StageActionRegistrySupportTest {
    @Test
    public void sortByOrderAsc() throws Exception {
        List<ILifeService> lifeServices = Lists.newArrayList();
        lifeServices.add(new C());
        lifeServices.add(new A0());
        lifeServices.add(new B());
        lifeServices.add(new A());

        List<ILifeService> sorted = StageActionRegistrySupport.sortByOrderAsc(lifeServices, Order.class);
        Assert.assertTrue(sorted.get(0).getClass() == A0.class);
        Assert.assertTrue(sorted.get(1).getClass() == A.class);
        Assert.assertTrue(sorted.get(2).getClass() == B.class);
        Assert.assertTrue(sorted.get(3).getClass() == C.class);

        sorted = StageActionRegistrySupport.sortByOrderAsc(lifeServices, ShutdownOrder.class);
        Assert.assertTrue(sorted.get(0).getClass() == C.class);
        Assert.assertTrue(sorted.get(1).getClass() == B.class);
        Assert.assertTrue(sorted.get(2).getClass() == A.class);
        Assert.assertTrue(sorted.get(3).getClass() == A0.class);
    }

    @ShutdownOrder(4)
    static class A0 extends LifeServiceSupport {

    }

    @Order(1)
    @ShutdownOrder(3)
    static class A extends LifeServiceSupport {

    }

    @Order(2)
    @ShutdownOrder(2)
    static class B extends LifeServiceSupport {

    }

    @Order(3)
    @ShutdownOrder(1)
    static class C extends LifeServiceSupport {

    }
}