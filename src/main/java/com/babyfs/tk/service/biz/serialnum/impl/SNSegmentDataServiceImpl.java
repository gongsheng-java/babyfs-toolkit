package com.babyfs.tk.service.biz.serialnum.impl;

import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.base.BaseDataServiceImpl;
import com.babyfs.tk.service.biz.cache.CacheParameter;
import com.babyfs.tk.service.biz.kvconf.ConfCacheConst;
import com.babyfs.tk.service.biz.serialnum.ISNSegmentDataService;
import com.babyfs.tk.service.biz.serialnum.dal.ISNSegmentDao;
import com.babyfs.tk.service.biz.serialnum.model.SNSegmentEntity;
import com.google.inject.Inject;

public class SNSegmentDataServiceImpl extends BaseDataServiceImpl<SNSegmentEntity> implements ISNSegmentDataService {
    private static final Class<SNSegmentEntity> CLAZZ = SNSegmentEntity.class;
    private static CacheParameter PARAM = ConfCacheConst.CONF_CACHE_PARAM;
    private final ISNSegmentDao dao;

    /**
     * @param dao        dao
     * @param daoFactory dao factory
     */
    @Inject
    public SNSegmentDataServiceImpl(ISNSegmentDao dao, DaoFactory daoFactory) {
        super(CLAZZ, dao, daoFactory, PARAM);
        this.dao = dao;
    }

    @Override
    public SNSegmentEntity getByType(int type) {
        return null;
    }
}
