package com.babyfs.tk.service.basic.guice;

import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceQueueKestrel;
import com.babyfs.tk.service.basic.queue.IQueue;
import com.babyfs.tk.service.basic.redis.IRedis;

import javax.inject.Inject;

/**
 * Class comments.
 * <p/>
 */
public class TestServiceImpl implements ITestService {

    @Inject
    @ServiceQueueKestrel
    private INameResourceService<IQueue> queueService;


    @Override
    public IQueue getQueueService(String name) throws Exception {
        return queueService.get(name);
    }

    @Override
    public IRedis getRedisService(String name) throws Exception {
        return null;
    }

}
