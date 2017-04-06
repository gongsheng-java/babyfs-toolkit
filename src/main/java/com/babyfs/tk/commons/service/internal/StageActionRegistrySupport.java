package com.babyfs.tk.commons.service.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.annotation.LifecycleServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.OrderUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 */
public class StageActionRegistrySupport implements IStageActionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("StageAction");
    private final List<Runnable> actions = Lists.newLinkedList();
    private boolean completed = false;

    @Inject
    @LifecycleServiceRegistry
    protected Set<ILifeService> lifeServices;

    @Override
    public synchronized void addAction(Runnable action) {
        Preconditions.checkState(!completed);
        Preconditions.checkArgument(action != null);
        LOGGER.info(this + " add action " + action);
        actions.add(action);
    }

    @Override
    public synchronized void execute() {
        if (completed) {
            LOGGER.warn("Actions has alerady completed,ignored. [" + this + "]");
            return;
        }
        completed = true;
        LOGGER.info("Run actions in " + this);
        for (Runnable action : actions) {
            try {
                LOGGER.info("Run action:" + action.getClass());
                action.run();
                LOGGER.info("Run action:" + action.getClass() + " finish");
            } catch (Exception e) {
                LOGGER.error("Execute action [" + action + "] fail.", e);
                throw new IllegalStateException("Exec stage action error.", e);
            }
        }
        LOGGER.info("Run actions in " + this + ",finish");
    }

    protected boolean isDisableStartLifeServcie() {
        return "true".equals(System.getProperty(Constants.DISABLE_START_LIFE_SERVCIE));
    }

    /**
     * 对{@link ILifeService}按{@link Order#value()}降序排序
     *
     * @param lifeServices
     * @return
     */
    protected static List<ILifeService> sortByOrderAsc(Collection<ILifeService> lifeServices) {
        if (lifeServices == null || lifeServices.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderedLifeService> orderedLifeServices = Lists.newArrayList();
        for (ILifeService lifeService : lifeServices) {
            Integer order = OrderUtils.getOrder(lifeService.getClass(), 0);
            orderedLifeServices.add(new OrderedLifeService(lifeService, order));
        }

        Collections.sort(orderedLifeServices);
        List<ILifeService> sorted = Lists.newArrayList();
        orderedLifeServices.forEach(ordered -> {
            sorted.add(ordered.lifeService);
        });

        return sorted;
    }

    /**
     * 对{@link ILifeService}按{@link Order#value()}升序排序
     *
     * @param lifeServices
     * @return
     */
    protected static List<ILifeService> sortByOrderDesc(Collection<ILifeService> lifeServices) {
        return Lists.reverse(sortByOrderAsc(lifeServices));
    }

    static class OrderedLifeService implements Comparable<OrderedLifeService> {
        private final ILifeService lifeService;
        private final Integer order;

        private OrderedLifeService(ILifeService lifeService, Integer order) {
            this.lifeService = Preconditions.checkNotNull(lifeService);
            this.order = Preconditions.checkNotNull(order);
        }

        @Override
        public int compareTo(OrderedLifeService other) {
            return this.order.compareTo(other.order);
        }
    }
}
