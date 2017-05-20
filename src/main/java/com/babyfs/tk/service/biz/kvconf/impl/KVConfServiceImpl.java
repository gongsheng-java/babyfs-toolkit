package com.babyfs.tk.service.biz.kvconf.impl;

import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.utils.ThreadUtil;
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
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.babyfs.tk.commons.model.ServiceResponse.*;

public class KVConfServiceImpl implements IKVConfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KVConfServiceImpl.class);
    private static final CacheParameter NAME_CACHE = ConfCacheConst.CONF_NAME_CACHE_PARAM;
    private static final ParsedEntity<KVConfEntity, Object> LOCAL_NULL_CONF = new ParsedEntity<>();

    private final IKVConfDataService dataService;
    private final IValidateService validateService;
    private final INameResourceService<IRedis> redisService;
    private final LoadingCache<String, ParsedEntity<KVConfEntity, Object>> localNameCache;
    private final LoadingCache<Long, ParsedEntity<KVConfEntity, Object>> localIdCache;

    @Inject(optional = true)
    RedisPubSubServcie pubSubService;

    @Inject
    public KVConfServiceImpl(IKVConfDataService dataService, IValidateService validateService, @ServiceRedis INameResourceService<IRedis> redisService, LocalCacheRegistry localCacheRegistry) {
        this.dataService = Preconditions.checkNotNull(dataService);
        this.validateService = Preconditions.checkNotNull(validateService);
        this.redisService = Preconditions.checkNotNull(redisService);
        localNameCache = buildLocalCacheBuilder().build(new CacheLoader<String, ParsedEntity<KVConfEntity, Object>>() {
            @Override
            public ParsedEntity<KVConfEntity, Object> load(String key) throws Exception {
                ServiceResponse<ParsedEntity<KVConfEntity, Object>> byName = getByName(key);
                if (byName.getData() == null) {
                    //如果返回的结果为空,则返回空对象代替
                    return LOCAL_NULL_CONF;
                }
                return byName.getData();
            }
        });
        localIdCache = buildLocalCacheBuilder().build(new CacheLoader<Long, ParsedEntity<KVConfEntity, Object>>() {
            @Override
            public ParsedEntity<KVConfEntity, Object> load(Long id) throws Exception {
                ServiceResponse<ParsedEntity<KVConfEntity, Object>> byId = get(id);
                if (byId.getData() == null) {
                    //如果返回的结果为空,则返回空对象代替
                    return LOCAL_NULL_CONF;
                }
                return byId.getData();
            }
        });

        localCacheRegistry.register(LocalCacheType.KV_CONF_NAME, localNameCache, LocalCacheRegistry.ToStringFunction);
        localCacheRegistry.register(LocalCacheType.KV_CONF_ID, localNameCache, LocalCacheRegistry.ToLongFunction);
    }


    @Override
    public ServiceResponse<KVConfEntity> add(KVConfEntity entity) {
        ServiceResponse response = checkAndFillEntity(entity);
        if (!response.isSuccess()) {
            //noinspection unchecked
            return response;
        }

        ServiceResponse<ParsedEntity<KVConfEntity, Object>> byName = this.getByName(entity.getName());
        if (byName.isSuccess() && byName.getData() != null) {
            return createFailResponse(ErrorCode.PARAM_ERROR, entity.getName() + "已经存在");
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

        return createFailResponse("新增配置失败");
    }

    @Override
    public ServiceResponse<Boolean> update(KVConfEntity entity) {
        ServiceResponse<ParsedEntity<KVConfEntity, Object>> get = this.get(entity.getId());
        if (!get.isSuccess() || get.getData() == null) {
            return createFailResponse(ErrorCode.PARAM_ERROR, "不存在");
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
    public ServiceResponse<ParsedEntity<KVConfEntity, Object>> get(long id) {
        if (id <= 0) {
            return createFailResponse(ErrorCode.PARAM_ERROR, "id应该大于0");
        }

        KVConfEntity entity = dataService.get(id);
        if (entity != null) {
            return createSuccessResponse(parse(entity));
        } else {
            return failResponse();
        }
    }

    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity, Object>> getWithLocalCache(long id) {
        try {
            ParsedEntity<KVConfEntity, Object> entity = this.localIdCache.get(id);
            if (entity != null && entity != LOCAL_NULL_CONF) {
                return createSuccessResponse(entity);
            }
        } catch (ExecutionException e) {
            LOGGER.error("get conf with `" + id + "` fail.", e);
        }
        return failResponse();
    }


    @Override
    public ServiceResponse<List<ParsedEntity<KVConfEntity, Object>>> getList(long[] ids) {
        if (ids == null || ids.length == 0) {
            return createFailResponse("ids is empty");
        }

        List<KVConfEntity> entities = this.dataService.getEntityListWithCache(ids);
        List<ParsedEntity<KVConfEntity, Object>> parsedEntityList = entities.stream().map(this::parse).collect(Collectors.toList());
        return createSuccessResponse(parsedEntityList);
    }

    @Override
    public ServiceResponse<List<ParsedEntity<KVConfEntity, Object>>> getListWithLocalCache(long[] ids) {
        if (ids == null || ids.length == 0) {
            return createFailResponse("ids is empty");
        }

        List<ParsedEntity<KVConfEntity, Object>> ret = Lists.newArrayListWithCapacity(ids.length);
        for (long id : ids) {
            ret.add(this.getWithLocalCache(id).getData());
        }
        return createSuccessResponse(ret);
    }

    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity, Object>> getByName(String name) {
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
            return createSuccessResponse(parse(entity));
        } else {
            return failResponse();
        }
    }

    @Override
    public ServiceResponse<ParsedEntity<KVConfEntity, Object>> getByNameWithLocalCache(String name) {
        try {
            ParsedEntity<KVConfEntity, Object> entity = this.localNameCache.get(name);
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
        KVConfEntity confEntity = this.dataService.get(id);
        if (confEntity == null) {
            return createFailResponse("无效的配置id");
        }
        try {
            return ResponseUtil.buildBoolResponse(this.dataService.del(confEntity));
        } finally {
            ThreadUtil.runQuitely(() -> {
                CacheUtils.delete(confEntity.getName(), NAME_CACHE, redisService);
            });
            this.invalidateLocalCache(confEntity);
        }
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
            return createFailResponse(ServiceResponse.FAIL_KEY, "无效的配置数据");
        }

        if (KVConfType.indexOf(entity.getType()) == null) {
            return createFailResponse(ServiceResponse.FAIL_KEY, "无效的配置类型");
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
                    return createFailResponse(ErrorCode.PARAM_ERROR, "无效的整数");
                }
            } else if (entity.getType() == KVConfType.DOUBLE.getIndex()) {
                try {
                    Double.parseDouble(content);
                } catch (Exception e) {
                    return createFailResponse(ErrorCode.PARAM_ERROR, "无效的浮点数");
                }
            } else if (entity.getType() == KVConfType.JSONOBJECT_TEXT.getIndex()) {
                final String jsonContent;
                KVConfJSONType kvConfJSONType = KVConfJSONType.get(entity.getName());
                if (kvConfJSONType != null) {
                    try {
                        jsonContent = JSONObject.toJSONString(JSONObject.parseObject(content, kvConfJSONType.getValueType()));
                    } catch (Exception e) {
                        return createFailResponse(ErrorCode.PARAM_ERROR, "无效的JSON值");
                    }
                } else {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        jsonContent = jsonObject.toJSONString();
                    } catch (Exception e) {
                        return createFailResponse(ErrorCode.PARAM_ERROR, "无效的JSON值");
                    }
                }
                entity.setContent(jsonContent);
            }
        }

        ValidateResult validateResult = validateService.validateParamByRule(ValidateConst.KVCONF_NAME, entity.getName(),
                ValidateConst.KVCONF_CONTENT, entity.getContent()
        );
        if (!validateResult.isSuccess()) {
            return createFailResponse(ErrorCode.PARAM_ERROR, validateResult.getErrorMsg());
        }
        return ServiceResponse.SUCCESS_RESPONSE;
    }

    @Override
    public ParsedEntity<KVConfEntity, Object> parse(KVConfEntity entity) {
        if (entity == null) {
            return null;
        }

        final ParsedEntity<KVConfEntity, Object> parsedKVConfEntity = new ParsedEntity<>();
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
        localIdCache.invalidate(toInvalidate.getId());
        //发送缓存变更通知
        if (pubSubService != null) {
            LocalCacheChangeMessage[] messages = new LocalCacheChangeMessage[]{
                    LocalCacheChangeMessage.newMessage(LocalCacheType.KV_CONF_NAME, toInvalidate.getName()),
                    LocalCacheChangeMessage.newMessage(LocalCacheType.KV_CONF_ID, toInvalidate.getId())
            };

            for (LocalCacheChangeMessage changeMessage : messages) {
                pubSubService.publish(pubSubService.getDeaultRedisGroup(), pubSubService.getDefaultChannelName(), JSONObject.toJSONString(changeMessage));
            }
        }
    }

    private CacheBuilder<Object, Object> buildLocalCacheBuilder() {
        return CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(10000);
    }
}
