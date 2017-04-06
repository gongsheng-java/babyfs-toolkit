package com.babyfs.tk.service.biz.factory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.babyfs.tk.commons.name.INameService;
import com.babyfs.tk.commons.name.Server;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.zookeeper.integration.guice.ZKClientModule;
import com.babyfs.tk.dal.guice.DalShardModule;
import com.babyfs.tk.dal.guice.DalXmlConfModule;
import com.babyfs.tk.rpc.guice.RPCClientModule;
import com.babyfs.tk.rpc.guice.RPCClientZkNSModule;
import com.babyfs.tk.rpc.guice.RPCServerModule;
import com.babyfs.tk.rpc.guice.RPCServerZkNSRegModule;
import com.babyfs.tk.service.basic.guice.BasicServiceConfModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModule;
import com.babyfs.tk.service.basic.guice.BasicServiceModuleProviders;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.security.guice.CryptoServiceModule;
import com.babyfs.tk.service.biz.service.parambean.intergration.guice.ParamBeanServiceModule;
import com.babyfs.tk.service.biz.service.validator.intergration.guice.ValidateServiceModule;
import redis.clients.jedis.JedisPool;

/**
 * 提供一些默认的静态共用Module , 主要是一些基础服务
 * <p/>
 */
public final class StaticModuleFactory {
    /**
     * RPC 服务端基础Module
     */
    public static final RPCClientModule BASE_MODULE_RPC_CLIENT = new RPCClientModule();

    /**
     * RPC 客户端基础Module
     */
    public static final RPCServerModule BASE_MODULE_RPC_SERVER = new RPCServerModule();

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
     * 默认的Zookeeper 服务端注册Module
     */
    public static final RPCServerZkNSRegModule BASE_MODULE_RPC_SERVER_ZK_NS_REG = new RPCServerZkNSRegModule();

    /**
     * 默认的Zookeeper 客户端注册Module
     */
    public static final RPCClientZkNSModule BASE_MODULE_RPC_CLIENT_ZK_NS = new RPCClientZkNSModule();

    /**
     * 默认的lifecycle Module
     */
    public static final LifecycleModule BASE_MODULE_LIFE_MODULE = new LifecycleModule();

    /**
     * 默认的测试用命名服务Module
     */
    public static final Module BASE_MODULE_NAMESERVICE_MODULE = new Module() {
        @Override
        public void configure(Binder binder) {
            binder.bind(INameService.class).toInstance(new BaseTestNameService());
        }
    };

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
     * 用于测试用的INameService实现 ：绑定本机 9123 端口
     */
    private static final class BaseTestNameService implements INameService {

        /**
         * 根据服务名称查询该服务的服务器,具体的策略由实现确认,可以考虑负载均衡等策略
         *
         * @param serviceName
         * @return
         */
        @Override
        public Server findServerByServiceName(String serviceName) {
            return new Server("test", "127.0.0.1", 9123);
        }

        @Override
        public Server findServerByServerId(String serviceName, String serverId) {
            return new Server("test", "127.0.0.1", 9123);
        }
    }


    /**
     * 数据校验服务
     * <p/>
     * 使用该服务时，需要通过
     * bindConstant().annotatedWith(Names.named(GlobalKeys.VALIDATION_RULE_CONF)).to(验证规则文件列表)
     * 来注入验证规则文件
     */
    public static final Module BASE_MODULE_VALIDATE_SERVICE = new ValidateServiceModule();

    /**
     * 非严格模式数据校验服务
     */
    public static final Module BASE_MODULE_VALIDATE_SERVICE_NO_STRICT = new ValidateServiceModule(false);

    /**
     * 请求参数bean处理服务，该服务依赖于{@link #BASE_MODULE_VALIDATE_SERVICE}
     * <p/>
     * 使用该服务时，需通过
     * bind(Class[].class).annotatedWith(Names.named(GlobalKeys.PARAM_BEAN_CLASSES)).toInstance(接口类数组)
     * 来注入需要进行处理的接口类
     */
    public static final Module BASE_MODULE_PARAM_BEAN_SERVICE = new ParamBeanServiceModule();

    /* 加密服务 */
    public static final CryptoServiceModule CRYPTO_SERVICE_MODULE = new CryptoServiceModule();

    private StaticModuleFactory() {

    }
}
