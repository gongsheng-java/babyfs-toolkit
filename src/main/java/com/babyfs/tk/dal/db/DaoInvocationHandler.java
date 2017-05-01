package com.babyfs.tk.dal.db;

import com.babyfs.tk.commons.JavaProxyUtil;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.dal.db.annotation.*;
import com.babyfs.tk.dal.db.funcs.EntityParameterFunc;
import com.babyfs.tk.dal.db.funcs.GetNumberFunction;
import com.babyfs.tk.dal.db.funcs.SetShardParameterFunc;
import com.babyfs.tk.dal.db.funcs.SetSqlParameterFunc;
import com.babyfs.tk.dal.meta.Shard;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.orm.IEntityMeta;
import com.babyfs.tk.probe.metrics.MetricsProbe;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dao调用的代理
 */
public final class DaoInvocationHandler implements InvocationHandler {
    private final Class<? extends IDao> daoClass;
    private final Class<? extends IEntity> entityClass;
    private final DaoSupport daoSupport;
    private final Map<Method, Function<Object[], Object>> methodFunctionMap = Maps.newHashMap();
    private final Class[] interfaces;
    private final String daoSimpleName;

    private final Function<Object[], Object> saveFunc = new Function<Object[], Object>() {
        @Override
        public Object apply(Object[] input) {
            return daoSupport.save((IEntity) input[0]);
        }
    };

    private final Function<Object[], Object> updateFunc = new Function<Object[], Object>() {
        @Override
        public Object apply(Object[] input) {
            return daoSupport.update((IEntity) input[0]);
        }
    };

    private final Function<Object[], Object> deleteFunc = new Function<Object[], Object>() {
        @Override
        public Object apply(Object[] input) {
            return daoSupport.delete((IEntity) input[0]);
        }
    };

    private final Function<Object[], Object> getFunc = new Function<Object[], Object>() {
        @Override
        public Object apply(Object[] input) {
            return daoSupport.get((Long) input[0], (Class) input[1]);
        }
    };

    /**
     * 解整数的参数
     */
    private static final Function<List<Object[]>, Object> GET_NUMBER_FUNC = new GetNumberFunction();


    public DaoInvocationHandler(Class<? extends IDao> daoClass, DaoSupport daoSupport, Class[] interfaces) {
        this.daoClass = daoClass;
        this.daoSupport = daoSupport;
        this.interfaces = interfaces;
        final Dao dao = this.daoClass.getAnnotation(Dao.class);
        this.entityClass = dao.entityClass();
        this.daoSimpleName = daoClass.getSimpleName();
        init();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 性能监控数据初始化
        final long st = System.nanoTime();
        boolean success = true;
        String itemName = daoSimpleName + "." + method.getName();
        try {
            Function<Object[], Object> objectFunction = methodFunctionMap.get(method);
            if (objectFunction != null) {
                return objectFunction.apply(args);
            } else {
                return JavaProxyUtil.invokeMethodOfObject(proxy, method, args, this.interfaces);
            }
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            MetricsProbe.timerUpdateNSFromStart("db", itemName, st, success);
        }
    }

    /**
     * 初始化
     */
    private void init() {
        try {
            //产生CRUD
            {
                //save
                Method save = IDao.class.getMethod("save", new Class[]{IEntity.class});
                methodFunctionMap.put(save, saveFunc);
                //update
                Method update = IDao.class.getMethod("update", new Class[]{IEntity.class});
                methodFunctionMap.put(update, updateFunc);
                //delete
                Method delete = IDao.class.getMethod("delete", new Class[]{IEntity.class});
                methodFunctionMap.put(delete, deleteFunc);
                //get
                Method get = IDao.class.getMethod("get", new Class[]{Long.TYPE, Class.class});
                methodFunctionMap.put(get, getFunc);
            }

            final Pair<IEntityMeta, IEntityHelper> metaPair = this.daoSupport.getEntityMetaSet().getMetaPair(this.entityClass);

            //扫描所有的方法
            Method[] methods = daoClass.getMethods();
            for (Method method : methods) {
                Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
                if (declaredAnnotations == null || declaredAnnotations.length == 0) {
                    continue;
                }
                int sqlDescCount = 0;
                final Sql sql = method.getAnnotation(Sql.class);
                if (sql != null) {
                    sqlDescCount++;
                }
                if (sqlDescCount == 0) {
                    continue;
                }
                Preconditions.checkState(sqlDescCount == 1, "Duplicate sql annotatoions for method %s", method);
                final SqlType type = sql.type();
                final List<SetSqlParameterFunc> setSqlParameterFuncs = generateParameterSetFuncs(method);
                List<SetShardParameterFunc> setShardParameterFuncs = generateShardSetFuncs(method);
                if (type == SqlType.QUERY_ENTITY) {
                    QueryEnityFunc func = new QueryEnityFunc(setSqlParameterFuncs, setShardParameterFuncs, sql.condition());
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.QUERY_COLUMNS) {
                    ColumnsFunc func = new ColumnsFunc(type, setSqlParameterFuncs, setShardParameterFuncs, sql.columns(), sql.condition(), null);
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.QUERY_COUNT) {
                    ColumnsFunc func = new ColumnsFunc(type, setSqlParameterFuncs, setShardParameterFuncs, sql.columns(), sql.condition(), GET_NUMBER_FUNC);
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.UPDATE_COLUMNS) {
                    ColumnsFunc func = new ColumnsFunc(type, setSqlParameterFuncs, setShardParameterFuncs, sql.columns(), sql.condition(), null);
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.DELETE) {
                    ColumnsFunc func = new ColumnsFunc(type, setSqlParameterFuncs, setShardParameterFuncs, sql.columns(), sql.condition(), null);
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.UPDATE_ENTITY) {
                    EntityParameterFunc entityParameterFunc = Preconditions.checkNotNull(generateEntityParameterFunc(method), "Can't find @EntityParam ");
                    UpdateEntityWithConditionFunc func = new UpdateEntityWithConditionFunc(entityParameterFunc, setSqlParameterFuncs, sql.condition());
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.EXEC) {
                    ExceFunc func = new ExceFunc(setSqlParameterFuncs, setShardParameterFuncs, sql.execSql(), sql.replaceTableName());
                    methodFunctionMap.put(method, func);
                } else if (type == SqlType.UPDATE_PARTIAL_ENTITY) {
                    EntityParameterFunc entityParameterFunc = Preconditions.checkNotNull(generateEntityParameterFunc(method), "Can't find @EntityParam ");
                    IEntityMeta entityMeta = metaPair.getFirst();
                    String updateColumns = entityMeta.paritalUpdateColumns(sql.includeColumns(), sql.excludeColumns());
                    UpdateParitialEntityWithConditionFunc func = new UpdateParitialEntityWithConditionFunc(entityParameterFunc, setSqlParameterFuncs, updateColumns, sql.condition());
                    methodFunctionMap.put(method, func);
                } else {
                    throw new UnsupportedOperationException("Unknown type:" + type);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Init proxy method for class " + this.daoClass, e);
        }

    }

    /**
     * 生成方法的参数与sql语句中命名参数之间的设置函数
     *
     * @param method
     * @return
     */
    private List<SetSqlParameterFunc> generateParameterSetFuncs(Method method) {
        final List<SetSqlParameterFunc> setSqlParameterFuncs = Lists.newArrayList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations != null && parameterAnnotations.length > 0) {
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations == null || annotations.length == 0) {
                    continue;
                }

                Set<Class<? extends Annotation>> annotationSet = Sets.newHashSet();
                boolean found = false;
                for (Annotation annotation : annotations) {
                    if (annotation != null) {
                        annotationSet.add(annotation.annotationType());
                        if (annotation.annotationType() == SqlParam.class) {
                            SetSqlParameterFunc func = new SetSqlParameterFunc(i, ((SqlParam) annotation).value());
                            setSqlParameterFuncs.add(func);
                            found = true;
                        }
                    }
                }
                if (found) {
                    Preconditions.checkArgument(!annotationSet.contains(EntityParam.class), "@SqlParam conflicts with @EntityParam");
                }
            }
        }
        return setSqlParameterFuncs;
    }

    /**
     * @param method
     * @return
     */
    private EntityParameterFunc generateEntityParameterFunc(Method method) {
        EntityParameterFunc entityParameterFunc = null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations != null && parameterAnnotations.length > 0) {
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations == null || annotations.length == 0) {
                    continue;
                }

                Set<Class<? extends Annotation>> annotationSet = Sets.newHashSet();
                boolean found = false;
                for (Annotation annotation : annotations) {
                    if (annotation != null) {
                        annotationSet.add(annotation.annotationType());
                        if (annotation.annotationType() == EntityParam.class) {
                            if (entityParameterFunc != null) {
                                throw new IllegalArgumentException("Only allow one @EntityParam in parameters");
                            }
                            entityParameterFunc = new EntityParameterFunc(i);
                            found = true;
                        }
                    }
                }

                if (found) {
                    Preconditions.checkArgument(!annotationSet.contains(SqlParam.class), "@EntityParam conflicts with @SqlParam");
                }
            }
        }
        return entityParameterFunc;
    }

    /**
     * 生成方法的参数与shard设置之间的设置函数
     *
     * @param method
     * @return
     */
    private List<SetShardParameterFunc> generateShardSetFuncs(Method method) {
        final List<SetShardParameterFunc> setSqlParameterFuncs = Lists.newArrayList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations != null && parameterAnnotations.length > 0) {
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations == null || annotations.length == 0) {
                    continue;
                }
                for (Annotation annotation : annotations) {
                    if (annotation != null) {
                        if (annotation.annotationType() == Shard.class) {
                            SetShardParameterFunc func = new SetShardParameterFunc(i, ((Shard) annotation).name());
                            setSqlParameterFuncs.add(func);
                        }
                    }
                }
            }
        }
        return setSqlParameterFuncs;
    }

    /**
     * 查询实体对象的Fucntion
     */
    private final class QueryEnityFunc implements Function<Object[], Object> {
        private final List<SetSqlParameterFunc> setSqlParameterFuncs;
        private final List<SetShardParameterFunc> setShardParameterFuncs;
        private final String condition;

        private QueryEnityFunc(@Nonnull List<SetSqlParameterFunc> setSqlParameterFuncs, List<SetShardParameterFunc> setShardParameterFuncs, @Nonnull String condition) {
            Preconditions.checkNotNull(setSqlParameterFuncs, "setSqlParameterFuncs");
            Preconditions.checkNotNull(condition, "condition");
            this.setSqlParameterFuncs = setSqlParameterFuncs;
            this.setShardParameterFuncs = setShardParameterFuncs;
            this.condition = condition;
        }

        @Override
        public Object apply(@Nullable Object[] input) {
            Pair<MapSqlParameterSource, Object[]> pair = new Pair<MapSqlParameterSource, Object[]>(new MapSqlParameterSource(), input);
            for (SetSqlParameterFunc func : setSqlParameterFuncs) {
                func.apply(pair);
            }

            Pair<Map<String, Object>, Object[]> shardPair = new Pair<Map<String, Object>, Object[]>(new HashMap<String, Object>(0), input);
            for (SetShardParameterFunc func : setShardParameterFuncs) {
                func.apply(shardPair);
            }

            return daoSupport.queryEntity(entityClass, condition, pair.first, shardPair.first);
        }
    }

    /**
     * 查询指定的列的Function
     */
    private final class ColumnsFunc implements Function<Object[], Object> {
        private final SqlType type;
        private final List<SetSqlParameterFunc> setSqlParameterFuncs;
        private final List<SetShardParameterFunc> setShardParameterFuncs;
        private final Function<List<Object[]>, Object> resultHandler;
        private final String columns;
        private final String condition;

        private ColumnsFunc(@Nonnull SqlType type, @Nonnull List<SetSqlParameterFunc> setSqlParameterFuncs, List<SetShardParameterFunc> setShardParameterFuncs, @Nonnull String columns, @Nonnull String condition, Function<List<Object[]>, Object> resultHandler) {
            Preconditions.checkNotNull(type, "type");
            Preconditions.checkNotNull(setSqlParameterFuncs, "setSqlParameterFuncs");
            Preconditions.checkNotNull(condition, "condition");
            Preconditions.checkNotNull(columns, "columns");
            this.type = type;
            this.setSqlParameterFuncs = setSqlParameterFuncs;
            this.setShardParameterFuncs = setShardParameterFuncs;
            this.columns = columns;
            this.condition = condition;
            this.resultHandler = resultHandler;
        }

        @Override
        public Object apply(@Nullable Object[] input) {
            Pair<MapSqlParameterSource, Object[]> pair = new Pair<MapSqlParameterSource, Object[]>(new MapSqlParameterSource(), input);
            for (SetSqlParameterFunc func : setSqlParameterFuncs) {
                func.apply(pair);
            }
            Pair<Map<String, Object>, Object[]> shardPair = new Pair<Map<String, Object>, Object[]>(new HashMap<String, Object>(0), input);
            for (SetShardParameterFunc func : setShardParameterFuncs) {
                func.apply(shardPair);
            }
            if (type == SqlType.QUERY_COLUMNS || type == SqlType.QUERY_COUNT) {
                List<Object[]> objects = daoSupport.queryEntityColumns(entityClass, columns, condition, pair.first, shardPair.first);
                if (resultHandler == null) {
                    return objects;
                } else {
                    return resultHandler.apply(objects);
                }
            } else if (type == SqlType.UPDATE_COLUMNS) {
                return daoSupport.update(entityClass, columns, condition, pair.first, shardPair.first);
            } else if (type == SqlType.DELETE) {
                return daoSupport.delete(entityClass, condition, pair.first, shardPair.first);
            } else {
                throw new UnsupportedOperationException("Unsupported type:" + type);
            }
        }
    }

    /**
     * 更新指定的实体
     */
    private final class UpdateEntityWithConditionFunc implements Function<Object[], Object> {
        private EntityParameterFunc entityParameterFunc;
        private final List<SetSqlParameterFunc> setSqlParameterFuncs;
        private final String condition;

        private UpdateEntityWithConditionFunc(@Nonnull EntityParameterFunc entityParameterFunc, @Nonnull List<SetSqlParameterFunc> setSqlParameterFuncs, @Nonnull String condition) {
            this.entityParameterFunc = Preconditions.checkNotNull(entityParameterFunc, "entityParameterFunc");
            this.setSqlParameterFuncs = Preconditions.checkNotNull(setSqlParameterFuncs, "setSqlParameterFuncs");
            this.condition = Preconditions.checkNotNull(condition, "condition");
        }

        @Override
        public Object apply(@Nullable Object[] input) {
            Pair<MapSqlParameterSource, Object[]> pair = new Pair<>(new MapSqlParameterSource(), input);
            for (SetSqlParameterFunc func : setSqlParameterFuncs) {
                func.apply(pair);
            }
            IEntity entity = Preconditions.checkNotNull(entityParameterFunc.apply(input));
            return daoSupport.update(entity, condition, pair.first);
        }
    }

    private final class ExceFunc implements Function<Object[], Object> {
        private final List<SetSqlParameterFunc> setSqlParameterFuncs;
        private final List<SetShardParameterFunc> setShardParameterFuncs;
        private final String execSql;
        private final boolean tableNameParaName;

        private ExceFunc(List<SetSqlParameterFunc> setSqlParameterFuncs, List<SetShardParameterFunc> setShardParameterFuncs, @Nonnull String execSql, boolean replaceTableName) {
            Preconditions.checkNotNull(setSqlParameterFuncs, "setSqlParameterFuncs");
            this.setSqlParameterFuncs = setSqlParameterFuncs;
            this.setShardParameterFuncs = setShardParameterFuncs;
            this.execSql = Preconditions.checkNotNull(execSql);
            this.tableNameParaName = replaceTableName;
        }

        @Override
        public Object apply(@Nullable Object[] input) {
            Pair<MapSqlParameterSource, Object[]> pair = new Pair<>(new MapSqlParameterSource(), input);
            for (SetSqlParameterFunc func : setSqlParameterFuncs) {
                func.apply(pair);
            }
            Pair<Map<String, Object>, Object[]> shardPair = new Pair<>(new HashMap<>(0), input);
            for (SetShardParameterFunc func : setShardParameterFuncs) {
                func.apply(shardPair);
            }
            return daoSupport.exec(entityClass, execSql, this.tableNameParaName, pair.first, shardPair.first);
        }
    }

    /**
     * 更新指定的实体的个别字段
     */
    private final class UpdateParitialEntityWithConditionFunc implements Function<Object[], Object> {
        private EntityParameterFunc entityParameterFunc;
        private final List<SetSqlParameterFunc> setSqlParameterFuncs;
        private final String updateColumns;
        private final String condition;


        private UpdateParitialEntityWithConditionFunc(@Nonnull EntityParameterFunc entityParameterFunc, @Nonnull List<SetSqlParameterFunc> setSqlParameterFuncs, @Nonnull String updateColumns, @Nonnull String condition) {
            this.entityParameterFunc = Preconditions.checkNotNull(entityParameterFunc, "entityParameterFunc");
            this.setSqlParameterFuncs = Preconditions.checkNotNull(setSqlParameterFuncs, "setSqlParameterFuncs");
            this.updateColumns = Preconditions.checkNotNull(updateColumns, "updateColumns");
            this.condition = Preconditions.checkNotNull(condition, "condition");
        }

        @Override
        public Object apply(@Nullable Object[] input) {
            Pair<MapSqlParameterSource, Object[]> pair = new Pair<>(new MapSqlParameterSource(), input);
            for (SetSqlParameterFunc func : setSqlParameterFuncs) {
                func.apply(pair);
            }
            IEntity entity = Preconditions.checkNotNull(entityParameterFunc.apply(input));
            return daoSupport.updatePartial(entity, this.updateColumns, this.condition, pair.first);
        }
    }

}
