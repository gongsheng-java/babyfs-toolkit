package com.babyfs.tk.service.biz.serialnum.impl;

import com.babyfs.tk.service.biz.counter.IDSequenceService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumService;
import com.babyfs.tk.service.biz.serialnum.ISerialNumServiceRegister;
import com.babyfs.tk.service.biz.serialnum.consts.SNCacheConst;
import com.babyfs.tk.service.biz.serialnum.enums.SerialNumType;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SerialNumServiceImpl implements ISerialNumService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialNumServiceImpl.class);

    private static final int CLIENT_MAX = 1 << 10;
    private static final long NUM_MAX = 1 << 24;

    @Inject
    private ISerialNumServiceRegister serviceRegister;

    @Inject
    private IDSequenceService sequenceService;

    @Override
    public String getSerialNum(SerialNumType type) {
        try {
            StringBuilder builder = new StringBuilder();
            //16位
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"));
            builder.append(date);
            int num = serviceRegister.getSerialNum();
            //4位
            int clientNum = (num & (CLIENT_MAX - 1)) | CLIENT_MAX;
            builder.append(clientNum);

            String key = SNCacheConst.SN_INCR_CACHE_PARAM.getCacheKey(num + "_" + type.getIndex());
            long id = sequenceService.getDailyNext(key, new Date());
            long newId = (id & (NUM_MAX - 1)) | NUM_MAX;
            //8位
            builder.append(newId);
            return builder.toString();
        } catch (Exception e) {
            LOGGER.error("create serial num error.", e);
            throw new RuntimeException(e);
        }
    }
}
