package com.babyfs.tk.service.biz.constants;

import com.babyfs.tk.commons.model.ServiceResponse;

/**
 * 通用的常量
 */
public class Const {
    /**
     * 发布订阅:默认的redis group组名
     */
    public static final String CONF_PUBSUB_DEFAULT_REDISGROUP = "pubsub.default.redisgroup";
    /**
     * 发布订阅:默认的channel name
     */
    public static final String CONF_PUBSUB_DEFAULT_CHANNELNAME = "pubsub.default.channelname";
    /**
     * 实体类型的EventBus名称
     */
    public static final String EVENTBUS_ENTITY = "entity";
    /**
     * 订阅类型的EventBus名称
     */
    public static final String EVENTBUS_SUBSCRIBE = "subscribe";
    /**
     * 用户相关的EventBus名称
     */
    public static final String EVENTBUS_USER = "user";
    /**
     * 系统属性配置,是否禁用事件通知
     */
    public static final String SYS_PROP_DISABLE_EVENT = "disable.event";
    /**
     * 删除标志
     */
    public static final String DEL_MARK = "_DEL_";
    /**
     * 执行后台任务的Executor名称,用于非关键的业务场景
     */
    public static final String NAME_BACKGROUND_EXECUTOR = "background_executor";
    /**
     * background表示异步构建,其他值表示同步构建
     */
    public static final String ES_INDEX_BACKGROUND_MODE = "background";
    /**
     * ES构建索引的模式
     *
     * @see #ES_INDEX_BACKGROUND_MODE
     */
    public static final String CONF_ES_INDEX_MODE = "es.index.mode";

    /**
     * 默认的add锁有效期
     */
    public static final int DEFAULT_ADD_LOCK_SECONDS = 6;

    /**
     * 参数错误的响应
     */
    public static final ServiceResponse PARAM_ERROR_RESPONSE = ServiceResponse.createFailResponse(ErrorCode.PARAM_ERROR, "参数错误");

    protected Const() {

    }
}
