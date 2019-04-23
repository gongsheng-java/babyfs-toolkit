package com.babyfs.tk.dal.db;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.db.shard.DBObjectSet;
import com.babyfs.tk.dal.db.shard.EntityShard;
import com.babyfs.tk.dal.db.shard.ShardUtil;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.orm.IEntityMeta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO支持基类,提供基本的CRUD操作
 */
public class DaoSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(DaoSupport.class);
    private static final DaoSupportConfig DEFAULT_SUPPORT_CONFIG = new DaoSupportConfig();

    static {
        //默认的查询超时30秒
        DEFAULT_SUPPORT_CONFIG.setQueryTimeout(30);
        //默认的Fectch size 50个
        DEFAULT_SUPPORT_CONFIG.setFetchSize(50);
    }

    private final DataSource dataSource;
    private final NamedParameterJdbcOperations namedJdbcTemplate;
    private final JdbcTemplate classicJdbcTemplate;
    private final EntityMetaSet entityMetaSet;
    private final DBObjectSet<EntityShard> entityShardSet;
    /**
     * 事务管理器
     */
    private final PlatformTransactionManager tm;

    /**
     * @param dataSource
     * @param entityMetaSet
     */
    public DaoSupport(DataSource dataSource, EntityMetaSet entityMetaSet) {
        this(dataSource, entityMetaSet, null);
    }

    /**
     * @param dataSource     JDBC数据源
     * @param entityMetaSet  实体类型
     * @param entityShardSet 实体的shard信息
     */
    public DaoSupport(DataSource dataSource, EntityMetaSet entityMetaSet, DBObjectSet<EntityShard> entityShardSet) {
        this(dataSource, entityMetaSet, entityShardSet, DEFAULT_SUPPORT_CONFIG);
    }

    /**
     * @param dataSource     JDBC数据源
     * @param entityMetaSet  实体类型
     * @param entityShardSet 实体的shard信息
     * @param config         DaoSupport的配置信息
     */
    public DaoSupport(DataSource dataSource, EntityMetaSet entityMetaSet, DBObjectSet<EntityShard> entityShardSet, DaoSupportConfig config) {
        Preconditions.checkNotNull(dataSource, "dataSource");
        Preconditions.checkNotNull(entityMetaSet, "entityMetaSet");
        this.dataSource = dataSource;
        this.entityMetaSet = entityMetaSet;
        this.entityShardSet = entityShardSet;
        this.classicJdbcTemplate = new JdbcTemplate(this.dataSource);
        if (config != null) {
            classicJdbcTemplate.setQueryTimeout(config.getQueryTimeout());
            classicJdbcTemplate.setFetchSize(config.getFetchSize());
        }
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(classicJdbcTemplate);
        tm = new DataSourceTransactionManager(this.dataSource);
    }

    /**
     * 执行一个原生事务操作
     * 该方法要求执行的sql中涉及到的表必须在同一个库中
     * <p/>
     * 注意:目前该方法不支持分表，以后考虑支持
     *
     * @param entityClass 实体类
     * @param shardValue  用于shard的值
     * @param func        业务相关回调方法
     * @param <T>         返回值类型
     * @return 业务回调方法的返回值
     */
    public <T, E extends IEntity> T doTransaction(Class<E> entityClass, @Nullable Map<String, Object> shardValue, final Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, T> func) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        try {
            setupDBShard(shardValue, metaPair);
            // 可通过TransactionTemplate来设置事务的隔离级别
            TransactionTemplate tt = new TransactionTemplate(tm);
            return tt.execute(new TransactionCallback<T>() {
                @Override
                public T doInTransaction(TransactionStatus transactionStatus) {
                    // 将TransactionStatus传入回调方法中，
                    // 使得可在回调方法中使用TransactionStatus.setRollbackOnly()来手动回滚，而不需要依赖抛出异常回滚
                    Pair<NamedParameterJdbcOperations, TransactionStatus> pair = Pair.of(namedJdbcTemplate, transactionStatus);
                    return func.apply(pair);
                }
            });
        } catch (Exception e) {
            throw new DALException("Shard[" + getCurrentDBShard() + "] ", e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 导出JdbcTemplate,提供给业务方法
     * <p/>
     * 注:该方法要求执行的sql中涉及到的表务必同时在同一DB实例中，而该DB实例由参数shardGuideClass和shardValue确定
     * 即:sql可以跨schema,但不能跨DB实例
     *
     * @param shardGuideClass 向导实体类,用作决定DbShard
     * @param shardValue      shard值
     * @param func            业务回调方法
     * @param <T>             返回值泛型
     * @return 业务方法的返回值，返回类型不做限制
     */
    public <T, E extends IEntity> T exposeJDBCTemplate(Class<E> shardGuideClass, @Nullable Map<String, Object> shardValue,
                                                       final Function<NamedParameterJdbcOperations, T> func) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(shardGuideClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + shardGuideClass);
        try {
            setupDBShard(shardValue, metaPair);
            return func.apply(this.namedJdbcTemplate);
        } catch (Exception e) {
            throw new DALException("Shard[" + getCurrentDBShard() + "] ", e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 直接使用JdbcTemplate,执行sql查询
     * <p/>
     * 注:该方法要求执行的sql中涉及到的表务必同时在同一DB实例中，而该DB实例由参数shardGuideClass和shardValue确定
     * 即:sql可以跨schema,但不能跨DB实例
     *
     * @param shardGuideClass 向导实体类,用作决定DbShard
     * @param shardValue      shard值
     * @param sql             查询sql语句
     * @param params          sql参数
     * @param <E>
     * @return 直接返回 {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate#getJdbcOperations()#queryForList}的返回值
     */
    public <E extends IEntity> List<Map<String, Object>> queryForList(Class<E> shardGuideClass, @Nullable Map<String, Object> shardValue,
                                                                      final String sql, final Object... params) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(shardGuideClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + shardGuideClass);
        try {
            setupDBShard(shardValue, metaPair);
            return (ObjectUtils.isEmpty(params) ?
                    namedJdbcTemplate.getJdbcOperations().queryForList(sql) :
                    namedJdbcTemplate.getJdbcOperations().queryForList(sql, getArguments(params)));
        } catch (Exception e) {
            throw new DALException("Shard[" + getCurrentDBShard() + "] ", e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 直接使用JdbcTemplate,执行sql查询
     * <p/>
     * 注:该方法要求执行的sql中涉及到的表务必同时在同一DB实例中，而该DB实例由参数shardGuideClass和shardValue确定
     * 即:sql可以跨schema,但不能跨DB实例
     *
     * @param shardGuideClass 向导实体类,用作决定DbShard
     * @param shardValue      shard值
     * @param sql             查询sql语句
     * @param params          sql参数
     * @param <E>
     * @return 直接返回 {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate#queryForList(String, Map)}的返回值
     */
    public <E extends IEntity> List<Map<String, Object>> queryForList(Class<E> shardGuideClass, @Nullable Map<String, Object> shardValue,
                                                                      final String sql, final Map<String, Object> params) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(shardGuideClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + shardGuideClass);
        try {
            setupDBShard(shardValue, metaPair);
            return namedJdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            throw new DALException("Shard[" + getCurrentDBShard() + "] ", e);
        } finally {
            cleanUpDBShard();
        }
    }


    /**
     * 保存一个实体对象
     *
     * @param entity
     * @return
     * @throws DALException
     * @throws IllegalArgumentException
     */
    public <T extends IEntity> T save(@Nonnull final T entity) {
        Preconditions.checkNotNull(entity, "entity");
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entity.getClass());
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entity.getClass());
        final IEntityMeta entityMeta = metaPair.first;
        final IEntityHelper helper = metaPair.second;
        try {
            setupDBShard(entity, metaPair);
            String tableName = getTableName(entity, metaPair);
            String sql = "insert into " + tableName + " (" + entityMeta.getInsertSqlColumns() + ") values (" + entityMeta.getInsertSqlColumnsValues() + ")";
            final SqlParameterSource source = helper.toSource(entity);
            if (!entityMeta.getIdField().isAutoId()) {
                Preconditions.checkArgument(entity.getId() > 0, "The subclass of AssignIdEntity must set an id which is greater than 0");
                namedJdbcTemplate.update(sql, source);
            } else {
                KeyHolder holder = new GeneratedKeyHolder();
                namedJdbcTemplate.update(sql, source, holder);
                long returnId = holder.getKey().longValue();
                entity.setId(returnId);
            }
            return entity;
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }


    /**
     * 更新一个实体
     *
     * @param entity
     * @return true, 更新成功;false,更新失败
     * @throws DALException
     * @throws IllegalArgumentException
     */
    public boolean update(@Nonnull IEntity entity) {
        Preconditions.checkArgument(entity.getId() > 0, "The id of the to updated entity must >0");
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entity.getClass());
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entity.getClass());
        final IEntityMeta entityMeta = metaPair.first;
        final IEntityHelper helper = metaPair.second;
        final String updateSqlColumns = entityMeta.getUpdateSqlColumns();
        final String idColumnName = entityMeta.getIdField().getColumnName();
        final String idAttribueName = entityMeta.getIdField().getAttribueName();
        try {
            String tableName = getTableName(entity, metaPair);
            setupDBShard(entity, metaPair);
            final String sql = "update " + tableName + " set " + updateSqlColumns + " where " + idColumnName + " =:" + idAttribueName;
            return namedJdbcTemplate.update(sql, helper.toSource(entity)) == 1;
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 删除指定的一个实体
     *
     * @param entity
     * @return true, 删除成功;false,删除失败
     * @throws DALException
     * @throws IllegalArgumentException
     */
    public boolean delete(@Nonnull IEntity entity) {
        Preconditions.checkArgument(entity.getId() > 0, "The id of the to updated entity must >0");
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entity.getClass());
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entity.getClass());
        final IEntityMeta entityMeta = metaPair.first;
        try {
            setupDBShard(entity, metaPair);
            String tableName = getTableName(entity, metaPair);
            String sql = "delete from " + tableName + " where " + entityMeta.getIdField().getColumnName() + " = ?";
            return namedJdbcTemplate.getJdbcOperations().update(sql, entity.getId()) == 1;
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 加载一个数据库的实体对象
     *
     * @param id
     * @param clazz
     * @param <T>
     * @return null, 没有找到与<code>id</code>对应的实体对象
     * @throws DALException
     * @throws IllegalArgumentException
     */
    public <T extends IEntity> T get(@Nonnegative long id, Class<T> clazz) {
        Preconditions.checkArgument(id > 0, "The id must be > 0");
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(clazz);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + clazz);
        final IEntityMeta entityMeta = metaPair.first;
        final IEntityHelper helper = metaPair.second;
        final String idColumnName = entityMeta.getIdField().getColumnName();
        String tableName = entityMeta.getTableName();
        try {
            if (entityMeta.getIdField().isShardField()) {
                //只有当实体的shard字段是id时才执行shard操作
                final Map<String, Object> shardValue = new HashMap<String, Object>(1);
                shardValue.put("id", id);
                setupDBShard(shardValue, metaPair);
                tableName = getTableName(shardValue, metaPair);
            } else {
                //无shard,使用默认的datasource
                setupDBShard(Collections.<String, Object>emptyMap(), metaPair);
            }
            final String sql = "select " + entityMeta.getQuerySqlColumns() + " from " + tableName + " where " + idColumnName + " = ?";
            List<Object> query = namedJdbcTemplate.getJdbcOperations().query(sql, new EntityRowMapper(helper), id);
            if (query == null || query.isEmpty()) {
                return null;
            }
            return (T) query.get(0);
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 执行实体的查询
     *
     * @param entityClass        实体的类型
     * @param conditiong         查询的条件
     * @param sqlParameterSource 参数
     * @param shardValue         用于shard的值
     * @param <T>
     * @return
     */
    public <T extends IEntity> List queryEntity(Class<T> entityClass, String conditiong, SqlParameterSource sqlParameterSource, Map<String, Object> shardValue) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        final IEntityMeta entityMeta = metaPair.first;
        final IEntityHelper helper = metaPair.second;
        try {
            setupDBShard(shardValue, metaPair);
            final String tableName = getTableName(shardValue, metaPair);
            final String sql = "select " + entityMeta.getQuerySqlColumns() + " from " + tableName + " " + (conditiong != null ? conditiong : "");
            return namedJdbcTemplate.query(sql, sqlParameterSource, new ObjectRowMapper2(helper));
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();

        }
    }

    /**
     * 执行实体指定的列
     *
     * @param entityClass        实体的类型
     * @param columns            查询的条件
     * @param conditiong         查询的条件
     * @param sqlParameterSource 参数
     * @param <T>
     * @return
     */
    public <T extends IEntity> List<Object[]> queryEntityColumns(@Nonnull Class<T> entityClass, @Nonnull String columns, String conditiong, SqlParameterSource sqlParameterSource, @Nullable Map<String, Object> shardValue) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        Preconditions.checkArgument(columns != null, "columns");
        try {
            setupDBShard(shardValue, metaPair);
            final String tableName = getTableName(shardValue, metaPair);
            final String sql = "select " + columns + " from " + tableName + " " + (conditiong != null ? conditiong : "");
            return (List) namedJdbcTemplate.query(sql, sqlParameterSource, new ColumnsRowMapper());
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 执行通用的Sql更新
     *
     * @param entityClass        实体的类型
     * @param columns            更新语句
     * @param sqlParameterSource 更新的参数
     * @param shardValue         用于shard的值
     * @return
     */
    public <T extends IEntity> int update(@Nonnull Class<T> entityClass, @Nonnull String columns, String conditiong, SqlParameterSource sqlParameterSource, @Nullable Map<String, Object> shardValue) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        Preconditions.checkArgument(columns != null, "columns");
        try {
            setupDBShard(shardValue, metaPair);
            final String tableName = getTableName(shardValue, metaPair);
            final String sql = "update " + tableName + " set  " + columns + " " + (conditiong != null ? conditiong : "");
            return namedJdbcTemplate.update(sql, sqlParameterSource);
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 执行通用的Sql更新
     *
     * @param entityClass        实体的类型
     * @param sql                更新语句
     * @param replaceTableName   是否替换sql中的表名
     * @param sqlParameterSource 更新的参数
     * @param shardValue         用于shard的值
     * @return
     */
    public <T extends IEntity> int exec(@Nonnull Class<T> entityClass, @Nonnull String sql, boolean replaceTableName, MapSqlParameterSource sqlParameterSource, @Nullable Map<String, Object> shardValue) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        Preconditions.checkArgument(sql != null, "sql");

        try {
            setupDBShard(shardValue, metaPair);
            final String tableName = getTableName(shardValue, metaPair);
            if (replaceTableName) {
                sql = String.format(sql, tableName);
            }
            return namedJdbcTemplate.update(sql, sqlParameterSource);
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    /**
     * 根据条件更新指定的实体
     *
     * @param entity             需要被更新的实体,非空
     * @param condition          更新条件,可以为空
     * @param conditionParameter 更新条件的参数,可以为空,参数的key不能与entity中的属性重名
     * @return
     */
    public boolean update(@Nonnull IEntity entity, String condition, MapSqlParameterSource conditionParameter) {
        return this.updateWithColumnsAndCondition(entity, null, condition, conditionParameter);
    }

    /**
     * 根据条件更新指定的实体的指定字段
     *
     * @param entity             需要被更新的实体,非空
     * @param updateColumsn      需要被更新的字段，非空
     * @param condition          更新条件,可以为空
     * @param conditionParameter 更新条件的参数,可以为空,参数的key不能与entity中的属性重名
     * @return
     */
    public boolean updatePartial(@Nonnull IEntity entity, String updateColumsn, String condition, MapSqlParameterSource conditionParameter) {
        return this.updateWithColumnsAndCondition(entity, updateColumsn, condition, conditionParameter);
    }


    /**
     * 执行通用的Sql更新
     *
     * @param entityClass        实体的类型
     * @param conditiong         删除的条件语句
     * @param sqlParameterSource 参数
     * @return
     */
    public <T extends IEntity> int delete(@Nonnull Class<T> entityClass, @Nonnull String conditiong, SqlParameterSource sqlParameterSource, @Nullable Map<String, Object> shardValue) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(conditiong), "condition");
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entityClass);
        Preconditions.checkArgument(metaPair != null, "Can't find the meta for the Entity " + entityClass);
        try {
            setupDBShard(shardValue, metaPair);
            final String tableName = getTableName(shardValue, metaPair);
            final String sql = "delete from " + tableName + " " + conditiong;
            return namedJdbcTemplate.update(sql, sqlParameterSource);
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    public EntityMetaSet getEntityMetaSet() {
        return entityMetaSet;
    }

    private <T extends IEntity> void setupDBShard(T shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        setupDBShard0(shardValue, pair);
    }

    private void setupDBShard(Map<String, Object> shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        setupDBShard0(shardValue, pair);
    }

    private void setupDBShard0(Object shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        if (!(this.dataSource instanceof ShardDataSource)) {
            return;
        }
        LOGGER.debug("setupDBShar,shardValue:{},metaPair:{}", shardValue, pair);
        if (this.entityShardSet != null) {
            final String entityName = pair.first.getEntityClass().getName();
            EntityShard entityShard = this.entityShardSet.get(entityName);
            LOGGER.debug("setupDBShar,shardValue:{},metaPair:{},entityShard:{}", shardValue, pair, entityShard);
            if (entityShard != null) {
                Map<String, Object> realShardValue = getRealShardValue(shardValue, pair);
                Preconditions.checkState(realShardValue != null, "Not a valid shard parameter type,must be Map<String,Object>,but it's %s", shardValue);
                String dbShardName = entityShard.findDBShardName(realShardValue);
                if (dbShardName != null) {
                    Pair<String, String> lookupKey = ShardUtil.createLookupKey(entityShard.getDbShardGroup(), dbShardName);
                    LOGGER.debug("setupDBShar,shardValue:{},metaPair:{},entityShard:{},lookupKey:{}", shardValue, pair, entityShard, lookupKey);
                    ShardUtil.setLookKey(lookupKey);
                    return;
                } else if (!realShardValue.isEmpty()) {
                    LOGGER.error("setupDBShard for {} with shardValue:{},but can't find shard name:{},", entityName, realShardValue);
                }
            }else {
                //shardGroup annotation
                Pair<String,String> groupPair = pair.first.getShardGroup();
                final String shardGroup = groupPair.first;
                if(!Strings.isNullOrEmpty(shardGroup)) {
                    LOGGER.debug("shardGroup annotation,shardValue:{},shardgroup:{}", shardValue, shardGroup);
                    Pair<String, String> lookupKey = ShardUtil.createLookupKey(shardGroup, groupPair.second);
                    LOGGER.debug("setupDBShar,shardValue:{},metaPair:{},lookupKey:{}", shardValue, pair, lookupKey);
                    ShardUtil.setLookKey(lookupKey);
                    return;
                }
            }
        }
        LOGGER.debug("setupDBShar,shardValue:{},metaPair:{},lookupKey:NULL_LOOKUP_KEY", shardValue, pair);
        ShardUtil.setLookKey(ShardUtil.NULL_LOOKUP_KEY);
    }

    private Map<String, Object> getRealShardValue(Object shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        Map<String, Object> realShardValue = null;
        if (shardValue instanceof IEntity) {
            if (pair.first.getShardFields() != null && !pair.first.getShardFields().isEmpty()) {
                realShardValue = pair.second.getShardValue((IEntity) shardValue);
            } else {
                realShardValue = Collections.emptyMap();
            }
        } else if (shardValue instanceof Map) {
            realShardValue = (Map<String, Object>) shardValue;
        }
        return realShardValue;
    }

    private <T extends IEntity> String getTableName(T shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        return getTableName0(shardValue, pair);
    }

    private String getTableName(Map<String, Object> shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        return getTableName0(shardValue, pair);
    }


    private String getTableName0(Object shardValue, Pair<IEntityMeta, IEntityHelper> pair) {
        if (!(this.dataSource instanceof ShardDataSource)) {
            return pair.first.getTableName();
        }
        LOGGER.debug("getTableName,shardValue:{},metaPair:{}", shardValue, pair);
        if (this.entityShardSet != null) {
            String entityName = pair.first.getEntityClass().getName();
            EntityShard entityShard = this.entityShardSet.get(entityName);
            LOGGER.debug("getTableName,shardValue:{},metaPair:{},entityShard:{}", shardValue, pair, entityShard);
            if (entityShard != null) {
                Map<String, Object> realShardValue = getRealShardValue(shardValue, pair);
                Preconditions.checkState(realShardValue != null, "Not a valid shard parameter type,must be Map<String,Object>.");
                String tableShardName = entityShard.findTableShardName(realShardValue);
                if (tableShardName != null) {
                    LOGGER.debug("getTableName,shardValue:{},metaPair:{},entityShard:{},tableShardName:{}", shardValue, pair, entityShard, tableShardName);
                    return tableShardName;
                } else if (!realShardValue.isEmpty()) {
                    LOGGER.error("getTableName for {} with shardValue:{},but can't find table name:{},", entityName, realShardValue);
                }
            }
        }
        LOGGER.debug("getTableName,shardValue:{},metaPair:{},entityShard:{},tableShardName:{}", shardValue, pair, null, pair.first.getTableName());
        return pair.first.getTableName();
    }

    private void cleanUpDBShard() {
        if (!(this.dataSource instanceof ShardDataSource)) {
            return;
        }
        ShardUtil.removeLookKey();
    }

    private String getCurrentDBShard() {
        Pair<String, String> lookKey = ShardUtil.getLookKey();
        if (lookKey == null || lookKey == ShardUtil.NULL_LOOKUP_KEY) {
            return "NoShard";
        } else {
            return "ShardGroup:" + lookKey.first + ",ShardId:" + lookKey.second;
        }
    }

    private DALException logAndReThrowException(Exception e) {
        String msg = "Shard[" + getCurrentDBShard() + "] ";
        LOGGER.error(msg, e);
        return new DALException(msg, e);
    }

    /**
     * Considers an Object array passed into a varargs parameter as
     * collection of arguments rather than as single argument.
     */
    private Object[] getArguments(Object[] varArgs) {
        if (varArgs.length == 1 && varArgs[0] instanceof Object[]) {
            return (Object[]) varArgs[0];
        } else {
            return varArgs;
        }
    }

    /**
     * 根据条件更新指定的实体
     *
     * @param entity             需要被更新的实体,非空
     * @param updateColumns
     * @param condition          更新条件,可以为空
     * @param conditionParameter 更新条件的参数,可以为空,参数的key不能与entity中的属性重名
     * @return
     */
    private boolean updateWithColumnsAndCondition(@Nonnull IEntity entity, String updateColumns, String condition, MapSqlParameterSource conditionParameter) {
        final Pair<IEntityMeta, IEntityHelper> metaPair = entityMetaSet.getMetaPair(entity.getClass());
        final IEntityMeta entityMeta = metaPair.first;
        final IEntityHelper helper = metaPair.second;
        final String updateSqlColumns = updateColumns == null ? entityMeta.getUpdateSqlColumns() : updateColumns;
        final String idColumnName = entityMeta.getIdField().getColumnName();
        final String idAttribueName = entityMeta.getIdField().getAttribueName();
        try {
            String tableName = getTableName(entity, metaPair);
            setupDBShard(entity, metaPair);
            condition = StringUtils.trimToEmpty(condition);
            if (!Strings.isNullOrEmpty(condition)) {
                condition = " AND " + condition;
            }

            final String sql = "update " + tableName + " set " + updateSqlColumns + " where " + idColumnName + " =:" + idAttribueName + condition;
            MapSqlParameterSource entityParameter = helper.toSource(entity);
            if (!Strings.isNullOrEmpty(condition) && conditionParameter != null && !conditionParameter.getValues().isEmpty()) {
                for (String key : conditionParameter.getValues().keySet()) {
                    if (entityParameter.hasValue(key)) {
                        throw new IllegalArgumentException("Can't set parameter [" + key + "],because it's already exist in entity property");
                    }
                }
                entityParameter.addValues(conditionParameter.getValues());
            }
            return namedJdbcTemplate.update(sql, entityParameter) == 1;
        } catch (Exception e) {
            throw logAndReThrowException(e);
        } finally {
            cleanUpDBShard();
        }
    }

    private static class EntityRowMapper implements RowMapper<Object> {
        private final IEntityHelper helper;

        public EntityRowMapper(IEntityHelper helper) {
            this.helper = helper;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return helper.toEntity(rs);
        }
    }

    private static class ObjectRowMapper2 implements RowMapper<Object> {
        private final IEntityHelper helper;

        public ObjectRowMapper2(IEntityHelper helper) {
            this.helper = helper;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return helper.toEntity(rs);
        }
    }

    private static class ColumnsRowMapper implements RowMapper<Object> {
        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] result = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                result[i - 1] = rs.getObject(i);
            }
            return result;
        }
    }
}
