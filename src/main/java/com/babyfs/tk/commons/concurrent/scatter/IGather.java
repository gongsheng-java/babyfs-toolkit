package com.babyfs.tk.commons.concurrent.scatter;

/**
 * 定义结果收集接口
 *
 * @param <INPUT>  输入数据的类型
 * @param <OUTPUT> 输出数据的类型
 */
public interface IGather<INPUT, OUTPUT> {
    /**
     * 追加结果集
     *
     * @param result
     */
    void append(INPUT result);

    /**
     * 取得最终的计算结果
     *
     * @return
     */
    OUTPUT get();
}
