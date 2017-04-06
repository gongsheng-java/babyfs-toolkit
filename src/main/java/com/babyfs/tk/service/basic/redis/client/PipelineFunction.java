package com.babyfs.tk.service.basic.redis.client;

import com.google.common.base.Function;
import org.elasticsearch.common.Strings;
import redis.clients.jedis.ShardedJedisPipeline;

/**
 * 一个基础的ShardJedisPipeline的抽象子类,所有项目中的redisPipeline均使用该类，勿使用父类
 * <p/>
 * <p/>
 * 针对ShardJedisPipeline提供了其他方法
 * <p/>
 */
public abstract class PipelineFunction implements Function<ShardedJedisPipeline, Void> {
    /**
     * 调用pipeline的方法名字 : 用于性能监控，有默认值
     */
    private final String name;

    /**
     * 默认前缀
     */
    private static final String PIPELINE_NAME_PREMIX = "pipeline_";

    /**
     *
     */
    public PipelineFunction() {
        this(null);
    }

    /**
     * @param pipelineName
     */
    public PipelineFunction(String pipelineName) {
        this.name = PIPELINE_NAME_PREMIX + (!Strings.isNullOrEmpty(pipelineName) ? pipelineName : "Unknown");
    }

    /**
     * 获得调用pipeline的来源方法名
     *
     * @return
     */
    public String getName() {
        return this.name;
    }
}

