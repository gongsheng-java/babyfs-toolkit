package com.babyfs.tk.service.biz.kvconf.guice;


import com.babyfs.tk.commons.service.ServiceModule;
import com.babyfs.tk.dal.guice.DalShardModule;
import com.babyfs.tk.service.biz.kvconf.IKVConfDataService;
import com.babyfs.tk.service.biz.kvconf.IKVConfService;
import com.babyfs.tk.service.biz.kvconf.dal.IKVConfDao;
import com.babyfs.tk.service.biz.kvconf.impl.KVConfDataServiceImpl;
import com.babyfs.tk.service.biz.kvconf.impl.KVConfServiceImpl;
import com.babyfs.tk.service.biz.kvconf.model.KVConfEntity;

/**
 * 键值配置
 */
public class KVConfServiceModule extends ServiceModule {

    @Override
    protected void configure() {
        DalShardModule.bindEntityAndDao(this.binder(), KVConfEntity.class, IKVConfDao.class);
        bindService(IKVConfDataService.class, KVConfDataServiceImpl.class);
        bindService(IKVConfService.class, KVConfServiceImpl.class);
    }
}
