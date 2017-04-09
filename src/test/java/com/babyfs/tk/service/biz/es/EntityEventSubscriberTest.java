package com.babyfs.tk.service.biz.es;

import com.babyfs.tk.service.biz.base.model.ChangeType;
import com.babyfs.tk.service.biz.base.model.EntityEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class EntityEventSubscriberTest {
    @Test
    public void handleStarEvent() throws Exception {
        Subscriber subscriber = new Subscriber();
        EventBus eventBus = new EventBus();
        eventBus.register(subscriber);
        eventBus.post(new EntityEvent<>(ChangeType.DEL, 1));
        eventBus.post(new EntityEvent<>(ChangeType.DEL, 1));
        Assert.assertEquals(2, subscriber.count);
    }


    public static class Subscriber {
        int count;

        @Subscribe
        @SuppressWarnings("unchecked")
        public void handleStarEvent(EntityEvent event) {
            System.out.println(event);
            count++;
        }
    }
}