package com.babyfs.tk.dal.guice;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.commons.xml.JAXBUtil;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.shard.DBInstance;
import com.babyfs.tk.dal.db.shard.DBShardInstance;
import com.babyfs.tk.dal.db.shard.EntityShard;
import com.babyfs.tk.dal.xml.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用xml配置DAL的Module
 */
public class DalXmlConfModule extends AbstractModule {
    private String dbInstanceXml;
    private String dbShardGroupXml;
    private String[] entityShardXmls;

    /**
     * @param dbInstanceXml   数据库实例的配置文件,必须非空
     * @param dbShardGroupXml 数据库的shard配置文件,必须非空
     * @param entityShardXml  数据库实体的shard配置文件,可以为空
     */
    public DalXmlConfModule(@Nonnull String dbInstanceXml, @Nonnull String dbShardGroupXml, String entityShardXml) {
        this(dbInstanceXml, dbShardGroupXml, entityShardXml != null ? new String[]{entityShardXml} : null);
    }

    /**
     * @param dbInstanceXml   数据库实例的配置文件,必须非空
     * @param dbShardGroupXml 数据库的shard配置文件,必须非空
     * @param entityShardXmls 数据库实体的shard配置文件,可以为空
     */
    public DalXmlConfModule(@Nonnull String dbInstanceXml, @Nonnull String dbShardGroupXml, String[] entityShardXmls) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dbInstanceXml), "dbInstanceXml");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dbShardGroupXml), "dbShardGroupXml");
        this.dbInstanceXml = dbInstanceXml;
        this.dbShardGroupXml = dbShardGroupXml;
        this.entityShardXmls = entityShardXmls;
    }

    @Override
    protected void configure() {
        {
            //注册数据库的实例集合
            XmlDBInstances xmlDbInstances = JAXBUtil.unmarshal(XmlDBInstances.class, dbInstanceXml);
            List<XmlServerInstance> instanceXmls = xmlDbInstances.getInstances();
            Multibinder<DBInstance> instanceSet = DalShardModule.createDBInstanceMutilbinder(binder());
            for (XmlServerInstance instanceXml : instanceXmls) {
                DBInstance dbInstance = new DBInstance(instanceXml.getId(), instanceXml.getIp(), instanceXml.getPort(), instanceXml.getUser(), instanceXml.getPassword());
                instanceSet.addBinding().toInstance(dbInstance);
            }
        }

        {
            //注册数据库shard实例集合
            XmlShardGroups xmlShardGroups = JAXBUtil.unmarshal(XmlShardGroups.class, dbShardGroupXml);
            Map<String, XmlShardGroup> groups = xmlShardGroups.getGroups();
            Multibinder<DBShardInstance> instanceSet = DalShardModule.createDBShardInstanceMutilbinder(binder());
            for (Map.Entry<String, XmlShardGroup> entry : groups.entrySet()) {
                XmlShardGroup value = entry.getValue();
                List<XmlShardInstance> xmlShardInstances = value.getShardInstances();
                for (XmlShardInstance instanceXml : xmlShardInstances) {
                    final List<Pair<String, String>> parameters;
                    {
                        if (instanceXml.getParameters() != null) {
                            parameters = instanceXml.getParameters()
                                    .stream()
                                    .filter(param -> param.getName() != null && param.getValue() != null)
                                    .map(param -> Pair.of(param.getName(), param.getValue()))
                                    .collect(Collectors.toList());
                        } else {
                            parameters = Collections.emptyList();
                        }
                    }
                    DBShardInstance dbShardInstance = new DBShardInstance(instanceXml.getId(), instanceXml.getDbInstanceId(), value.getId(), instanceXml.getSchema(), parameters);
                    instanceSet.addBinding().toInstance(dbShardInstance);
                }
            }
            XmlShardGroups.DefaultGroup defaultShard = xmlShardGroups.getDefaultShard();
            if (defaultShard != null) {
                String groupId = defaultShard.getGroupId();
                String shardId = defaultShard.getShardId();
                DalShardModule.bindDefaultShard(binder(), groupId, shardId);
            }
        }

        {
            //注册数据库实体shard集合
            Multibinder<EntityShard> instanceSset = DalShardModule.createEntityShardMutilbinder(binder());
            if (entityShardXmls != null) {
                Set<String> existedEntityShard = Sets.newHashSet();
                for (String entityShardXml : entityShardXmls) {
                    XmlEntityShards xmlEntityShards = JAXBUtil.unmarshal(XmlEntityShards.class, entityShardXml);
                    List<XmlEntityShards.XmlEntityShard> entityShards1 = xmlEntityShards.getEntityShards();
                    if (null != entityShards1) {
                        for (XmlEntityShards.XmlEntityShard entityShard : entityShards1) {
                            XmlEntityShards.ShardStrategyType dbShardStrategies = entityShard.getDbShardStrategies();
                            List<EntityShard.IShardStrategy> dbStrategies = ListUtil.transform(dbShardStrategies.getShardStrategies(), new Func());
                            XmlEntityShards.ShardStrategyType tableShardStrategies = entityShard.getTableShardStrategies();
                            List<EntityShard.IShardStrategy> tableStrategies = ListUtil.transform(tableShardStrategies.getShardStrategies(), new Func());
                            Preconditions.checkState(existedEntityShard.add(entityShard.getClassName()), "Duplicate entity shard %s in %s", entityShard.getClassName(), entityShardXml);
                            try {
                                EntityShard es = new EntityShard((Class<? extends IEntity>) Class.forName(entityShard.getClassName()), entityShard.getGroups(), dbStrategies, tableStrategies);
                                instanceSset.addBinding().toInstance(es);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class Func implements Function<XmlShardStrategy, EntityShard.IShardStrategy> {
        @Override
        public EntityShard.IShardStrategy apply(@Nonnull XmlShardStrategy input) {
            String type = input.getType();
            Map<String, String> properties = input.getProperties();
            if ("hash".equals(type)) {
                int sharCount = Integer.parseInt(properties.get("shardCount"));
                String prefixName = properties.get("shardNamePrefix");
                return new EntityShard.HashShardStrategy(sharCount, prefixName);
            } else if ("named".equals(type)) {
                String name = properties.get("shardName");
                return new EntityShard.NamedShardStrategy(name);
            } else {
                throw new RuntimeException("Unknown strategy type:" + type);
            }
        }

    }
}
