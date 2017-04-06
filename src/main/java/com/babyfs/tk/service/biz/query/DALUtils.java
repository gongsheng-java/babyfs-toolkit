package com.babyfs.tk.service.biz.query;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.orm.IEntity;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.dal.db.IDao;
import com.babyfs.tk.service.biz.cache.BaseCacheDataService;
import com.babyfs.tk.service.biz.cache.utils.CacheParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DAL查询结果处理的一些工具方法
 */
public final class DALUtils {
    private DALUtils() {

    }

    /**
     * 检查List是否不为空
     * <p/>
     * 1.针对：普通list
     * 2.针对：DAL QUERY_COLUMNS 的响应的结果 List<Object[]>
     *
     * @param list
     * @return true 不为空
     */
    public static boolean checkListNotNullOrEmpty(List list) {
        if (list == null || list.isEmpty()) {
            return false;
        } else {
            // 检查DAL响应的结果 List<Object[]>
            Object value = list.get(0);
            if (value instanceof Object[]) {
                Object[] objects = (Object[]) value;
                if (objects.length == 0 || objects[0] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int extractInt(List<Object[]> input, int defaultValue) {
        if (input == null || input.isEmpty()) {
            return defaultValue;
        }
        return ((Number) input.get(0)[0]).intValue();
    }

    /**
     * 解析第一个字段：用于解析出第一个id
     *
     * @param input
     * @param <T>
     * @return
     */
    public static <T> T extractFirst(List<Object[]> input) {
        if (input != null && !input.isEmpty()) {
            Object[] seqObjArr = input.get(0);
            if (seqObjArr != null) {
                Object obj = seqObjArr[0];
                if (obj != null) {
                    return (T) obj;
                }
            }
        }
        return null;
    }

    /**
     * 从结果集中解析出指定column的列表
     *
     * @param input
     * @param columnIndex
     * @param <T>
     * @return
     */
    @SuppressWarnings(value = {"unchecked"})
    public static <T> List<T> extractColumn(List<Object[]> input, int columnIndex) {
        if (input == null || input.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }
        List<T> ret = Lists.newArrayListWithCapacity(input.size());
        for (Object[] obj : input) {
            Preconditions.checkState(obj != null && obj.length > columnIndex);
            ret.add((T) obj[columnIndex]);
        }
        return ret;
    }

    /**
     * 基于缓存查询分页数据,默认的排序条件未<code>id DESC</code>
     *
     * @param daoFactory
     * @param dalContext
     * @param pageParams
     * @param conditionPair
     * @param <E>
     * @return
     */
    public static <E extends IEntity> PageResult<E> queryByPage(EntityDALContext<E> dalContext,
                                                                DaoFactory daoFactory,
                                                                BaseCacheDataService dataService,
                                                                PageParams pageParams,
                                                                final Pair<String, Map<String, Object>> conditionPair) {
        return queryByPage(dalContext, daoFactory, dataService, pageParams, conditionPair, "id DESC");
    }

    /**
     * @param dalContext
     * @param daoFactory
     * @param dataService
     * @param pageParams
     * @param conditionPair
     * @param orderCondition 排序条件
     * @param <E>
     * @return
     */
    public static <E extends IEntity> PageResult<E> queryByPage(EntityDALContext<E> dalContext,
                                                                DaoFactory daoFactory,
                                                                BaseCacheDataService dataService,
                                                                PageParams pageParams,
                                                                Pair<String, Map<String, Object>> conditionPair,
                                                                String orderCondition) {
        Preconditions.checkNotNull(pageParams, "Invalid pageParams");
        Preconditions.checkNotNull(conditionPair, "Invalid conditionPair");
        Preconditions.checkNotNull(conditionPair.first, "Invalid conditionPair first sql");
        Preconditions.checkNotNull(conditionPair.second, "Invalid conditionPair second params map.");

        int count = -1;
        //如果需要分页,先查询总数
        if (pageParams.getLimit() > 0) {
            final String countCondition = conditionPair.first;
            final MapSqlParameterSource countQueryParams = new MapSqlParameterSource(conditionPair.second);
            List<Object[]> countColumns = daoFactory.getDaoSupport().queryEntityColumns(
                    dalContext.getEntityClass(), "count(id)",
                    countCondition, countQueryParams, Collections.<String, Object>emptyMap());
            count = extractInt(countColumns, 0);
        }

        List<E> entities;
        if (count == -1 || count > 0) {
            String pageCondition = conditionPair.first;
            if (!Strings.isNullOrEmpty(orderCondition)) {
                pageCondition += " ORDER BY " + orderCondition;
            }
            MapSqlParameterSource pageQueryParams = new MapSqlParameterSource(conditionPair.second);
            if (pageParams.getLimit() > 0) {
                //分页查询数据
                pageCondition += " LIMIT :from, :size";
                pageQueryParams.addValue("from", pageParams.getBeginIndex());
                pageQueryParams.addValue("size", pageParams.getLimit());
            }
            List<Object[]> idColumns = daoFactory.getDaoSupport().queryEntityColumns(
                    dalContext.getEntityClass(), "id",
                    pageCondition, pageQueryParams, Collections.<String, Object>emptyMap());
            List<Long> ids = extractColumn(idColumns, 0);
            if (count == -1) {
                count = ids.size();
            }
            entities = dataService.queryEntitiesWithCache(ids, dalContext.getEntityClass(), dalContext.getDao(), dalContext.getCacheParameter());
        } else {
            entities = Collections.emptyList();
        }
        return new PageResult<>(pageParams.getPage(), pageParams.getLimit(), count, entities);
    }

    /**
     * 构建Entity相关上下文
     *
     * @param entityClass
     * @param dao
     * @param cacheParameter
     * @param <E>
     * @return
     */
    public static <E extends IEntity> EntityDALContext<E> buildEntityDALContext(Class<E> entityClass, IDao<E> dao, CacheParameter cacheParameter) {
        return new EntityDALContext<E>(entityClass, dao, cacheParameter);
    }

    /**
     * 封装的基于缓存Entity操作需要的参数
     *
     * @param <E>
     */
    public static class EntityDALContext<E extends IEntity> {

        private Class<E> entityClass;
        private IDao<E> dao;
        private CacheParameter cacheParameter;

        public EntityDALContext(Class<E> entityClass, IDao<E> dao, CacheParameter cacheParameter) {
            this.entityClass = entityClass;
            this.dao = dao;
            this.cacheParameter = cacheParameter;
        }

        public Class<E> getEntityClass() {
            return entityClass;
        }

        public void setEntityClass(Class<E> entityClass) {
            this.entityClass = entityClass;
        }

        public IDao<E> getDao() {
            return dao;
        }

        public void setDao(IDao<E> dao) {
            this.dao = dao;
        }

        public CacheParameter getCacheParameter() {
            return cacheParameter;
        }

        public void setCacheParameter(CacheParameter cacheParameter) {
            this.cacheParameter = cacheParameter;
        }
    }

}
