package com.babyfs.tk.service.biz;

import com.babyfs.tk.commons.service.IVersion;
import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.commons.service.VersionModule;
import com.babyfs.tk.galaxy.guice.MethodCacheServiceModel;
import com.babyfs.tk.galaxy.guice.RpcOkHttpClientModel;
import com.babyfs.tk.galaxy.guice.RpcSupportModel;
import com.babyfs.tk.galaxy.guice.ZkDiscoveryClientModule;
import com.babyfs.tk.galaxy.server.IRpcService;
import com.babyfs.tk.galaxy.server.impl.RpcServiceImpl;
import com.babyfs.tk.http.guice.HttpClientModule;
import com.babyfs.tk.service.basic.es.guice.ESClientModule;
import com.babyfs.tk.service.biz.constants.Const;
import com.babyfs.tk.service.biz.schedule.guice.ExecutorServiceModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.zookeeper.integration.guice.ZKClientModule;
import com.babyfs.tk.dal.guice.DalShardModule;
import com.babyfs.tk.dal.guice.DalXmlConfModule;
import com.babyfs.tk.service.basic.guice.BasicServiceConfModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModuleProviders;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.security.guice.CryptoServiceModule;
import com.babyfs.tk.service.biz.validator.guice.ValidateServiceModule;
import redis.clients.jedis.JedisPool;

/**
 * 常用的Module , 主要是一些基础服务
 * <p/>
 */
public final class Modules {
    /**
     * 默认的DB XML Config Module
     */
    public static final DalXmlConfModule BASE_MODULE_DB_XML_CONF = new DalXmlConfModule("db_instance.xml", "shard_instance.xml", "entity_shards.xml");

    /**
     * 默认的DAL SHARD MODULE
     */
    public static final DalShardModule BASE_MODULE_DAL_SHARD = new DalShardModule();

    /**
     * 默认的Zookeeper 客户端Module
     */
    public static final ZKClientModule BASE_MODULE_ZK_CLIENT = new ZKClientModule();

    /**
     * 默认的lifecycle Module
     */
    public static final LifecycleModule BASE_MODULE_LIFE_MODULE = new LifecycleModule();

    /**
     * 默认的Redis服务配置Module
     */
    public static final BasicServiceConfModule BASIC_MODULE_REDIS_CONF = new BasicServiceConfModule() {
        @Override
        protected void configure() {
            // 初始化redis配置
            bindXmlConfByAnnotation(ServiceRedis.class, "redis-servers.xml", "redis-client.xml");
        }
    };

    /**
     * 默认的Redis服务Module
     */
    public static final BasicServiceModule BASIC_MODULE_REDIS_SERVICE = new BasicServiceModule() {
        @Override
        protected void configure() {
            // 初始化redis配置
            bindBasicService(ServiceRedis.class, IRedis.class, BasicServiceModuleProviders.ShardedRedisServiceProvider.class);
            // 增加JedisPool配置
            bindBasicService(ServiceRedis.class, JedisPool.class, BasicServiceModuleProviders.JedisPoolServiceProvider.class);
        }
    };


    /**
     * 数据校验服务
     * <p/>
     * 使用该服务时，需要通过
     * bindConstant().annotatedWith(Names.named(GlobalKeys.VALIDATION_RULE_CONF)).to(验证规则文件列表)
     * 来注入验证规则文件
     */
    public static final Module BASE_MODULE_VALIDATE_SERVICE = new ValidateServiceModule();

    /* 加密服务 */
    public static final CryptoServiceModule CRYPTO_SERVICE_MODULE = new CryptoServiceModule();

    /**
     * 公共的核心Module CORE_MODULES
     */
    public static final ImmutableList<Module> CORE_MODULES = buildCoreModules();

    private Modules() {

    }

    /**
     * 构建基础的Module
     *
     * @return
     */
    private static ImmutableList<Module> buildCoreModules() {
        return ImmutableList.<Module>builder()
                .add(new BaseServiceModule())
                .add(BASE_MODULE_DB_XML_CONF)
                .add(BASE_MODULE_DAL_SHARD)
                .add(BASIC_MODULE_REDIS_CONF)
                .add(BASIC_MODULE_REDIS_SERVICE)
                .add(BASE_MODULE_VALIDATE_SERVICE)
                .add(CRYPTO_SERVICE_MODULE)
                .build();
    }


    /**
     * 基础和业务无关的服务Module
     */
    private static class BaseServiceModule extends ServiceModule {
        @Override
        protected void configure() {
            DalShardModule.createEntityClassMutilbinder(this.binder());
            bindService(IVersion.class, VersionModule.VersionImpl.class);
            install(new ESClientModule(null, "_es."));
            install(new HttpClientModule());

            //一般的后台任务Executor,用于非关键的业务场景
            install(new ExecutorServiceModule(Const.NAME_BACKGROUND_EXECUTOR));

            //rpc module
            install(new RpcSupportModel());
        }
    }
}
