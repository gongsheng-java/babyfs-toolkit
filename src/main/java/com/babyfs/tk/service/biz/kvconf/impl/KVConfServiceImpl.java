package com.babyfs.tk.service.biz.kvconf.impl;

import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.validator.ValidateResult;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.base.model.ParsedEntity;
import com.babyfs.tk.service.biz.cache.*;
import com.babyfs.tk.service.biz.constants.ErrorCode;
import com.babyfs.tk.service.biz.constants.ValidateConst;
import com.babyfs.tk.service.biz.kvconf.*;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
import com.babyfs.tk.service.biz.pubsub.RedisPubSubServcie;
import com.babyfs.tk.service.biz.validator.IValidateService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.babyfs.tk.commons.model.ServiceResponse.createSuccessResponse;
import static com.babyfs.tk.commons.model.ServiceResponse.failResponse;

public class KVConfServiceImpl implements IKVConfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KVConfServiceImpl.class);
    private static final CacheParameter NAME_CACHE = ConfCacheConst.CONF_NAME_CACHE_PARAM;
    private static final ParsedEntity<KVConfEntity> LOCAL_NULL_CONF = new ParsedEntity<>();

    private final IKVConfDataService dataService;
    private final IValidateService validateService;
    private final INameResourceService<IRedis> redisService;
    private final LoadingCache<String, ParsedEntity<KVConfEntity>> localNameCache;

    @Inject(optional = true)
    RedisPubSubServcie pubSubService;

    @Inject
    public KVConfServiceImpl(IKVConfDataService dataService, IValidateService validateService, @ServiceRedis INameResourceService<IRedis> redisService, LocalCacheRegistry localCacheRegistry) {
        this.dataService = Preconditions.checkNotNull(dataService);
        this.validateService = Preconditions.checkNotNull(validateService);
        this.redisService = Preconditions.checkNotNull(redisService);
        localNameCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(10000).build(new CacheLoader<String, ParsedEntity<KVConfEntity>>() {
            @Override
            public ParsedEntity<KVConfEntity> load(String key) throws Exception {
                ServiceResponse<ParsedEntity<KVConfEntity>> byName = getByName(key);
                if (byName.getData() == null) {
                    //如果返回的结果为空,则返回空对象代替
                    return LOCAL_NULL_CONF;
                }
                return byName.getData();
            }
        });
        localCacheRegistry.register(LocalCacheType.KV_CONF, localNameCache, LocalCacheRegistry.ToStringFunction);
    }

    @Override
    public ServiceResponse<KVConfEntity> add(KVConfEntity entity) {
        ServiceResponse response = checkAndFillEntity(entity);
        if (!response.isSuccess()) {
            //noinspection unchecked
            return response;
        }

        ServiceResponse<ParsedEntity<KVConfEntity>> byName = this.getByName(entity.getName());
        if (byName.isSuccess() && byName.getData() != null) {
            return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "已经存在");
        }

        KVConfEntity add = null;
        try {
            add = dataService.add(entity);
            if (add != null && add.getId() > 0) {
                return createSuccessResponse(add);
            }
        } catch (Exception e) {
            LOGGER.error("add kvconf fail", e);
        } finally {
            invalidateLocalCache(add);
        }

        return ServiceResponse.createFailResponse("新增配置失败");
    }

    @Override
    public ServiceResponse<Boolean> update(KVConfEntity entity) {
        ServiceResponse<ParsedEntity<KVConfEntity>> get = this.get(entity.getId());
        if (!get.isSuccess() || get.getData() == null) {
            return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "不存在");
        }

        // 仅能修改内容
        KVConfEntity toUpdate = get.getData().getEntity();
        toUpdate.setContent(entity.getContent());

        ServiceResponse response = checkAndFillEntity(toUpdate);
        if (!response.isSuccess()) {
            //noinspection unchecked
            return response;
        }

        try {
            boolean ret = dataService.update(toUpdate);
            return ResponseUtil.buildBoolResponse(ret);
        } finally {
            //删除缓存
            CacheUtils.delete(toUpdate.getName(), NAME_CACHE, redisService);
            invalidateLocalCache(toUpdate);
        }
    }


    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity>> get(long id) {
        if (id <= 0) {
            return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "id应该大于0");
        }

        KVConfEntity entity = dataService.get(id);
        if (entity != null) {
            return createSuccessResponse(parseContent(entity));
        } else {
            return failResponse();
        }
    }

    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity>> getByName(String name) {
        KVConfEntity entity;
        do {
            entity = CacheUtils.get(name, NAME_CACHE, redisService);
            if (entity != null) {
                break;
            }

            entity = this.dataService.getByName(name);
            if (entity != null) {
                CacheUtils.set(name, entity, NAME_CACHE, redisService);
                break;
            }
        } while (false);

        if (entity != null) {
            return createSuccessResponse(parseContent(entity));
        } else {
            return failResponse();
        }
    }

    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity>> getByNameWithLocalCache(String name) {
        try {
            ParsedEntity<KVConfEntity> entity = this.localNameCache.get(name);
            if (entity != null && entity != LOCAL_NULL_CONF) {
                return createSuccessResponse(entity);
            }
        } catch (ExecutionException e) {
            LOGGER.error("get conf with `" + name + "` fail.", e);
        }
        return failResponse();
    }


    @Override
    public ServiceResponse<Boolean> del(long id) {
        return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "不能删除配置");
    }

    @Override
    public boolean isSysName(String name) {
        return KVConfJSONType.isSysName(name);
    }

    /**
     * 检查参数,并填充必要的数据
     *
     * @param entity entity 非空
     * @return
     */
    private ServiceResponse checkAndFillEntity(KVConfEntity entity) {
        if (entity == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的配置数据");
        }

        if (KVConfType.indexOf(entity.getType()) == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "无效的配置类型");
        }

        // name统一转为小写
        if (!Strings.isNullOrEmpty(entity.getName())) {
            entity.setName(entity.getName().toLowerCase());
        }


        // 允许内容为空
        String content = entity.getContent();
        if (!Strings.isNullOrEmpty(content)) {
            if (entity.getType() == KVConfType.INTEGER.getIndex()) {
                try {
                    Long.parseLong(content);
                } catch (Exception e) {
                    return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "无效的整数");
                }
            } else if (entity.getType() == KVConfType.DOUBLE.getIndex()) {
                try {
                    Double.parseDouble(content);
                } catch (Exception e) {
                    return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "无效的浮点数");
                }
            } else if (entity.getType() == KVConfType.JSONOBJECT_TEXT.getIndex()) {
                final String jsonContent;
                KVConfJSONType kvConfJSONType = KVConfJSONType.get(entity.getName());
                if (kvConfJSONType != null) {
                    try {
                        jsonContent = JSONObject.toJSONString(JSONObject.parseObject(content, kvConfJSONType.getValueType()));
                    } catch (Exception e) {
                        return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "无效的JSON值");
                    }
                } else {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        jsonContent = jsonObject.toJSONString();
                    } catch (Exception e) {
                        return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "无效的JSON值");
                    }
                }
                entity.setContent(jsonContent);
            }
        }

        ValidateResult validateResult = validateService.validateParamByRule(ValidateConst.KVCONF_NAME, entity.getName(),
                ValidateConst.KVCONF_CONTENT, entity.getContent()
        );
        if (!validateResult.isSuccess()) {
            return ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, validateResult.getErrorMsg());
        }
        return ServiceResponse.SUCCESS_RESPONSE;
    }

    /**
     * 根据类型解析内容
     *
     * @param entity
     */
    private ParsedEntity<KVConfEntity> parseContent(KVConfEntity entity) {
        final ParsedEntity<KVConfEntity> parsedKVConfEntity = new ParsedEntity<>();
        parsedKVConfEntity.setEntity(entity);

        String content = entity.getContent();
        if (Strings.isNullOrEmpty(content)) {
            return parsedKVConfEntity;
        }

        if (entity.getType() == KVConfType.INTEGER.getIndex()) {
            parsedKVConfEntity.setParsed(Integer.parseInt(content));
        } else if (entity.getType() == KVConfType.DOUBLE.getIndex()) {
            parsedKVConfEntity.setParsed(Double.parseDouble(content));
        } else if (entity.getType() == KVConfType.JSONOBJECT_TEXT.getIndex()) {
            KVConfJSONType jsonType = KVConfJSONType.get(entity.getName());
            if (jsonType == null) {
                parsedKVConfEntity.setParsed(JSONObject.parseObject(content));
            } else {
                parsedKVConfEntity.setParsed(JSONObject.parseObject(content, jsonType.getValueType()));
            }
        }

        return parsedKVConfEntity;
    }

    private void invalidateLocalCache(KVConfEntity toInvalidate) {
        if (toInvalidate == null || toInvalidate.getName() == null) {
            return;
        }
        //删除本地
        localNameCache.invalidate(toInvalidate.getName());
        //发送缓存变更通知
        if (pubSubService != null) {
            LocalCacheChangeMessage message = LocalCacheChangeMessage.newMessage(LocalCacheType.KV_CONF, toInvalidate.getName());
            pubSubService.publish(pubSubService.getDeaultRedisGroup(), pubSubService.getDefaultChannelName(), JSONObject.toJSONString(message));
        }
    }
}
