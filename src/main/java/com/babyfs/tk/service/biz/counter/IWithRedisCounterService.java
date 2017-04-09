package com.babyfs.tk.service.biz.counter;

import com.babyfs.tk.service.biz.counter.impl.RedisCounterService;

import java.util.Set;

public interface IWithRedisCounterService {
    Set<RedisCounterService> getRedisCounterService();
}
