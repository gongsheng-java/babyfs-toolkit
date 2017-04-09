package com.babyfs.tk.service.biz.verify.impl;

import com.alibaba.fastjson.JSON;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.cache.CacheUtils;
import com.babyfs.tk.service.biz.verify.ISmsVerifyCodeService;
import com.babyfs.tk.service.biz.verify.SmsCodeParameter;
import com.babyfs.tk.service.biz.verify.VerifyCacheConst;
import com.babyfs.tk.service.biz.verify.VerifyCode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * 验证码发送、校验服务类
 */
public class SmsVerifyCodeServiceImpl implements ISmsVerifyCodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsVerifyCodeServiceImpl.class);
    private static final CacheParameter CACHE_PARAM = VerifyCacheConst.SMS_CODE_CACHE_PARAM;
    private static final String FORMAT_KEY = "%d_%s";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    @ServiceRedis
    private INameResourceService<IRedis> redisService;

    @Override
    public String createCode(SmsCodeParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter must not be null.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(parameter.getMobile()), "mobile must not be null or empty");
        Preconditions.checkArgument(parameter.getSecond() > 0, "second must be > 0");

        final String redisKey = getRedisKey(parameter.getType(), parameter.getMobile());
        Object obj = CacheUtils.get(redisKey, CACHE_PARAM, redisService, false);
        VerifyCode verify = parseVerify(obj);

        int code;
        if (verify != null) {
            code = verify.getCode();
        } else {
            // 生成6位验证码
            code = generateSmsCode();
        }

        if (verify == null) {
            IRedis redis = CacheUtils.getRedisCacheClient(redisService, CACHE_PARAM.getRedisServiceGroup());
            verify = new VerifyCode(code, false);
            redis.setObject(CACHE_PARAM.getCacheKey(redisKey), JSON.toJSONString(verify), parameter.getSecond());
        }

        return String.valueOf(verify.getCode());
    }


    @Override
    public boolean checkCode(SmsCodeParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter mustn't be null.");
        String mobile = StringUtils.trimToNull(parameter.getMobile());
        String toCheckCode = StringUtils.trimToNull(parameter.getToCheckCode());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobile), "phone is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toCheckCode), "checkCode is null");

        final int type = parameter.getType();
        String redisKey = getRedisKey(type, mobile);
        Object obj = CacheUtils.get(redisKey, CACHE_PARAM, redisService, false);
        VerifyCode verify = parseVerify(obj);

        boolean pass = false;
        if (verify != null) {
            pass = Objects.equals(toCheckCode, String.valueOf(verify.getCode()));
            verify.setVerified(pass);
        }

        //如果验证通过,并且需要记录验证状态,重新设置
        if (pass && parameter.isRecordCheckPass()) {
            if (parameter.getRecordCheckPassSecond() > 0) {
                IRedis redis = CacheUtils.getRedisCacheClient(redisService, CACHE_PARAM.getRedisServiceGroup());
                redis.setObject(CACHE_PARAM.getCacheKey(redisKey), JSON.toJSONString(verify), parameter.getRecordCheckPassSecond());
            } else {
                LOGGER.warn("The recordCheckPassSechond is <=0,skip set it", parameter.getRecordCheckPassSecond());
            }
        }
        return pass;
    }

    @Override
    public boolean checkCodeVerified(SmsCodeParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter mustn't be null.");
        String mobile = StringUtils.trimToNull(parameter.getMobile());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobile), "phone is null");

        int type = parameter.getType();
        String redisKey = getRedisKey(type, mobile);
        Object obj = CacheUtils.get(redisKey, CACHE_PARAM, redisService, false);
        VerifyCode verify = parseVerify(obj);
        return verify != null && verify.isVerified();
    }

    /**
     * 删除缓存
     *
     * @param parameter request type  phone
     * @return
     */
    @Override
    public void delete(SmsCodeParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter mustn't be null.");
        String mobile = StringUtils.trimToNull(parameter.getMobile());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobile), "mobile is null");
        int type = parameter.getType();
        String redisKey = getRedisKey(type, mobile);
        CacheUtils.delete(redisKey, CACHE_PARAM, redisService);
    }

    /**
     * 获取存储在Redis中的键值
     *
     * @param type  短信发送类型
     * @param phone 手机号
     * @return key
     */
    protected String getRedisKey(int type, String phone) {
        Preconditions.checkNotNull(phone);
        return String.format(FORMAT_KEY, type, phone);
    }

    /**
     * @param obj
     * @return
     */
    private VerifyCode parseVerify(Object obj) {
        VerifyCode verify = null;
        String strValue = (obj instanceof String) ? (String) obj : null;
        if (strValue != null) {
            verify = JSON.parseObject(strValue, VerifyCode.class);
        }
        return verify;
    }

    /**
     * 生成短信验证码
     */
    protected Integer generateSmsCode() {
        return RANDOM.nextInt(899999) + 100000;
    }
}
