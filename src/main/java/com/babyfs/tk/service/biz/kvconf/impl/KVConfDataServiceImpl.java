package com.babyfs.tk.service.biz.kvconf.impl;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.DalUtil;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.base.BaseDataServiceImpl;
import com.babyfs.tk.service.biz.base.query.PageParams;
import com.babyfs.tk.service.biz.base.query.PageResult;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.kvconf.ConfCacheConst;
import com.babyfs.tk.service.biz.kvconf.dal.IKVConfDao;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;
import com.babyfs.tk.service.biz.kvconf.model.KVConfQuery;
import com.babyfs.tk.service.biz.kvconf.IKVConfDataService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class KVConfDataServiceImpl extends BaseDataServiceImpl<KVConfEntity> implements IKVConfDataService {
    private static final Class<KVConfEntity> CLAZZ = KVConfEntity.class;
    private static CacheParameter PARAM = ConfCacheConst.CONF_CACHE_PARAM;
    private final IKVConfDao dao;

    /**
     * @param dao        dao
     * @param daoFactory dao factory
     */
    @Inject
    public KVConfDataServiceImpl(IKVConfDao dao, DaoFactory daoFactory) {
        super(CLAZZ, dao, daoFactory, PARAM);
        this.dao = dao;
    }

    @Override
    public KVConfEntity getByName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }
        List<KVConfEntity> entities = dao.queryOneByName(name);
        if (ListUtil.isEmpty(entities)) {
            return null;
        }
        return entities.get(0);
    }

    @Override
    public PageResult<KVConfEntity> query(PageParams pageParams, KVConfQuery queryParam) {
        Preconditions.checkNotNull(pageParams, "Invalid pageParams");
        Preconditions.checkNotNull(queryParam, "Invalid queryParam");

        //构造基本的查询条件
        Pair<String, Map<String, Object>> conditionPair;
        {
            final Map<String, Object> params = Maps.newHashMap();
            StringBuilder conditionSql = new StringBuilder();

            if (queryParam.getId() > 0) {
                conditionSql.append(" AND id = :id");
                params.put("id", queryParam.getId());
            }

            if (!Strings.isNullOrEmpty(queryParam.getName())) {
                String nameLike = queryParam.getName() + "%";
                conditionSql.append(" AND name like :name");
                params.put("name", nameLike);
            }

            if (queryParam.getStat() > -1) {
                conditionSql.append(" AND stat = :status");
                params.put("status", queryParam.getStat());
            }

            if (queryParam.getDel() > -1) {
                conditionSql.append(" AND del = :del");
                params.put("del", queryParam.getDel());
            }

            String conditionPrefix = !params.isEmpty() ? " where 1=1 " : "";
            String sql = conditionPrefix + conditionSql.toString();
            conditionPair = Pair.of(sql, params);
        }
        String orderCondition = "id DESC";
        DalUtil.EntityDALContext<KVConfEntity> dalContext = DalUtil.buildEntityDALContext(KVConfEntity.class, dao, cacheParam);
        return DalUtil.queryByPage(dalContext, daoFactory, this, pageParams, conditionPair, orderCondition);
    }
}
