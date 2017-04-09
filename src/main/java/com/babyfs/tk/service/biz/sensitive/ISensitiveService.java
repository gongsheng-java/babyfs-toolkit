package com.babyfs.tk.service.biz.sensitive;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.service.ILifeService;

/**
 * 敏感词服务
 */
public interface ISensitiveService extends ILifeService {
    /**
     * 是否包含敏感词
     *
     * @param text
     * @return true, 包含;false,不包含
     */
    boolean hasSensitiveWords(String text);

    /**
     * 过滤敏感词
     *
     * @param text
     * @return 返回过滤后的结果
     */
    ServiceResponse<String> filterSensitiveWords(String text);
}
