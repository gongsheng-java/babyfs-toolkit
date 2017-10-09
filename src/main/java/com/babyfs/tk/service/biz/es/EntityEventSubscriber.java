package com.babyfs.tk.service.biz.es;

import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.service.biz.base.IEntityESService;
import com.babyfs.tk.service.biz.base.IEntityPubService;
import com.babyfs.tk.service.biz.base.model.ChangeType;
import com.babyfs.tk.service.biz.base.model.EntityEvent;
import com.babyfs.tk.service.biz.constants.Const;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 实体变更订阅
 */
public class EntityEventSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityEventSubscriber.class);

    private final Map<Class, IEntityESService> esHandlers;
    private final Map<Class, IEntityPubService> pubHandlers;
    private final boolean backgroundES;

    @Inject
    @Named(Const.NAME_BACKGROUND_EXECUTOR)
    ExecutorService backgroundTaskExecutor;

    @Inject
    public EntityEventSubscriber(@Named(Const.EVENTBUS_ENTITY) EventBus eventBus,
                                 @Named(Const.EVENTBUS_ENTITY) Map<Class, IEntityESService> esHandlers,
                                 @Named(Const.EVENTBUS_ENTITY) Map<Class, IEntityPubService> pubHandlers,
                                 IConfigService configService) {
        Preconditions.checkNotNull(eventBus);
        this.esHandlers = Preconditions.checkNotNull(esHandlers);
        this.pubHandlers = Preconditions.checkNotNull(pubHandlers);
        this.backgroundES = Const.ES_INDEX_BACKGROUND_MODE.equals(configService.get(Const.CONF_ES_INDEX_MODE));
        LOGGER.info("es index background mode:{}", this.backgroundES);
        eventBus.register(this);
    }

    @Subscribe
    @AllowConcurrentEvents
    @SuppressWarnings("unchecked")
    public void handlEvent(EntityEvent event) {
        if (event == null) {
            LOGGER.warn("Receive null starEvent");
            return;
        }
        LOGGER.info("event:{}", event);
        handlePub(event);
        if (backgroundES) {
            try {
                backgroundTaskExecutor.submit(() -> {
                    handleES(event);
                });
            } catch (Exception e) {
                LOGGER.error("submit es index task fail.", e);
            }
        } else {
            handleES(event);
        }
    }


    /**
     * 处理ES更新
     *
     * @param event
     */
    @SuppressWarnings("unchecked")
    private void handleES(EntityEvent event) {
        try {
            IEntityESService esService = this.esHandlers.get(event.getClass());
            if (esService == null) {
                LOGGER.error("Can't find hanler for event:" + event);
                return;
            }

            ChangeType changeType = event.getChangeType();
            if (changeType == ChangeType.ADD || event.isUpdateAsAddForES()) {
                esService.index(event.getEntity(), event.getAttach());
            } else if (changeType == ChangeType.UPDATE) {
                esService.update(event.getEntity(), event.getAttach());
            } else if (changeType == ChangeType.DEL) {
                esService.delete(event.getId());
            }
        } catch (Exception e) {
            LOGGER.error("handler event `" + event + "` fail", e);
        }
    }


    /**
     * 处理订阅发布
     *
     * @param event
     */
    private void handlePub(EntityEvent event) {
        try {
            IEntityPubService service = this.pubHandlers.get(event.getClass());
            if (service == null) {
                return;
            }

            ChangeType changeType = event.getChangeType();
            if (changeType == ChangeType.ADD) {
                service.add(event.getEntity(), event.getAttach());
            } else if (changeType == ChangeType.UPDATE) {
                service.update(event.getEntity(), event.getAttach());
            } else if (changeType == ChangeType.DEL) {
                service.delete(event.getEntity());
            }
        } catch (Exception e) {
            LOGGER.error("handler event `" + event + "` fail", e);
        }
    }

    /**
     * 记录Dead event
     *
     * @param deadEvent
     */
    @Subscribe
    public void deadEvent(DeadEvent deadEvent) {
        LOGGER.warn("receive dead event:{}", deadEvent);
    }
}
