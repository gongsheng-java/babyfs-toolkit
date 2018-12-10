package com.babyfs.tk.service.biz.freq;


/**
 * 频率频次相关服务
 */
public interface IFreqService {

    /**
     * 检查，并且更新频率频次
     *
     * @param key
     * @param freqParameter
     * @return true, 频次检查通过;false,不通过
     */
    boolean checkAndUpdate(String key, FreqParameter freqParameter);

    /**
     * 检查频次
     *
     * @param key
     * @param freqParameter
     * @return true, 频次检查通过;false,不通过
     */
    boolean check(String key, FreqParameter freqParameter);

    /**
     * 清除缓存数据
     *
     * @param key
     * @param freqParameter
     */
    void clean(String key, FreqParameter freqParameter);

    /**
     * 查看剩余时间
     *
     * @param key
     * @param freqParameter
     * @return
     */
    long getTTL(String key, FreqParameter freqParameter);
}
