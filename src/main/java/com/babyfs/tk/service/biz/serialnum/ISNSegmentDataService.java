package com.babyfs.tk.service.biz.serialnum;

import com.babyfs.tk.service.biz.base.IDataService;
import com.babyfs.tk.service.biz.serialnum.model.SNSegmentEntity;

/**
 * {@link SNSegmentEntity}的数据服务接口
 */
public interface ISNSegmentDataService extends IDataService<SNSegmentEntity> {

    /**
     * 根据type查询
     *
     * @param type 类型
     * @return
     */
    SNSegmentEntity getByType(int type);
}
