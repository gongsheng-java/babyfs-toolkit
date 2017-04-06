package com.babyfs.tk.servcie.basic.guice;

import com.babyfs.tk.service.basic.queue.IQueue;
import com.babyfs.tk.service.basic.redis.IRedis;

/**
 * Class comments.
 * <p/>
 */
public interface ITestService {

    public IQueue getQueueService(String name) throws Exception;


    public IRedis getRedisService(String name) throws Exception;

}
