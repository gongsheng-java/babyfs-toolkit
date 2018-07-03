package com.babyfs.tk.service.biz.serialnum.impl;

import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.babyfs.tk.service.biz.serialnum.ISerialNumService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumServiceRegister;
import com.babyfs.tk.service.biz.serialnum.consts.SNCacheConst;
import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;
import com.google.inject.Inject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SerialNumServiceImpl implements ISerialNumService {
    private static final int CLIENT_MAX = 1 << 10;
    private static final long NUM_MAX = 1 << 24;

    @Inject
    private ISerialNumServiceRegister serviceRegister;

    @Inject
    @ServiceRedis
    private INameResourceService<IRedis> redisService;

    @Override
    public String getSerialNum(SerialNumType type) {
        StringBuilder builder = new StringBuilder();
        //16位
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSS"));
        builder.append(date);
        int num = serviceRegister.getSerialNum();
        //4位
        int clientNum = (num & (CLIENT_MAX -1)) | CLIENT_MAX;
        builder.append(clientNum);

        IRedis redis = CacheUtils.getRedisCacheClient(redisService, SNCacheConst.SN_INCR_CACHE_PARAM.getRedisServiceGroup());
        String key = SNCacheConst.SN_INCR_CACHE_PARAM.getCacheKey(num + "_" + type.getIndex());
        Long id = redis.incr(key);
        long newId = (id & (NUM_MAX -1)) | NUM_MAX;
        //8位
        builder.append(newId);
        return builder.toString();
    }
}
