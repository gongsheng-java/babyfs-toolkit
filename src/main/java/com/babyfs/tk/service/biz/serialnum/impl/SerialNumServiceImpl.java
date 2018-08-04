package com.babyfs.tk.service.biz.serialnum.impl;

import com.babyfs.tk.service.biz.counter.IDSequenceService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumServiceRegister;
import com.babyfs.tk.service.biz.serialnum.consts.SNCacheConst;
import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SerialNumServiceImpl implements ISerialNumService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialNumServiceImpl.class);

    private static final int CLIENT_MAX = 1 << 6;
    private static final int CLIENT_MIN = 1 << 5;
    private static final long NUM_MAX = 1 << 19;
    private static final long NUM_MIN = 1 << 18;
    private static final int TYPE_MAX = 1 << 6;
    private static final int TYPE_MIN = 1 << 5;

    private static final long MILL_NUM_MAX = 1 << 15;
    private static final long MILL_NUM_MIN = 1 << 14;
    private long tempMillis;
    private long millCount;

    @Inject
    private ISerialNumServiceRegister serviceRegister;

    @Inject
    private IDSequenceService sequenceService;

    @Override
    public String getSerialNum(SerialNumType type) {
        try {
            StringBuilder builder = new StringBuilder();
            //12位
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            builder.append(date);
            int num = serviceRegister.getSerialNum();
            //2位
            int clientNum = (num & (CLIENT_MAX - 1)) | CLIENT_MIN;
            builder.append(clientNum);
            //2位
            int numType = (type.getIndex() & (TYPE_MAX - 1)) | TYPE_MIN;
            builder.append(numType);

            String key = SNCacheConst.SN_INCR_CACHE_PARAM.getCacheKey(num + "_" + type.getIndex());
            long id = sequenceService.getDailyNext(key, new Date());
            long newId = (id & (NUM_MAX - 1)) | NUM_MIN;
            //6位
            builder.append(newId);
            return builder.toString();
        } catch (Exception e) {
            LOGGER.error("create serial num error.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSFSerialNum(SerialNumType type) {
        int num = serviceRegister.getSerialNum();
        int clientNum = (num & (CLIENT_MAX - 1)) | CLIENT_MIN;
        StringBuilder builder = new StringBuilder();
        //22位
        builder.append(getSnowflakeNum());
        //4位
        builder.append(clientNum);
        //2位
        int numType = (type.getIndex() & (TYPE_MAX - 1)) | TYPE_MIN;
        builder.append(numType);
        return builder.toString();
    }

    /**
     * nowflake算法
     * @return
     */
    private synchronized String getSnowflakeNum(){
        long millis = System.currentTimeMillis();
        //同一毫秒递增
        if (millis == tempMillis) {
            millCount++;
            //超过MILL_NUM_MAX，则阻塞获取下一秒
            if ((millCount & (MILL_NUM_MIN - 1)) == 0L) {
                millis = getNextMillSecond(millis);
            }
        }else {
            millCount = 0L;
        }
        tempMillis = millis;

        StringBuilder builder = new StringBuilder();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Asia/Shanghai"));
        //17位
        String date = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        builder.append(date);
        //5位
        long newId = (millCount & (MILL_NUM_MAX - 1)) | MILL_NUM_MIN;
        builder.append(newId);
        return builder.toString();
    }

    /**
     * 阻塞直到获取下一毫秒
     * @param millis
     * @return
     */
    private long getNextMillSecond(long millis) {
        for (;;) {
            long current = System.currentTimeMillis();
            if (current > millis) {
                return current;
            }
        }
    }

}