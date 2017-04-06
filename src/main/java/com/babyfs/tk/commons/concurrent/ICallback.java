package com.babyfs.tk.commons.concurrent;

/**
 * 通用的回调接口
 *
 * @param <CONTEXT> 操作完成时的回调上下文类型
 * @param <RESULT>  操作成果执行时的结果类型
 * @see {@link CallbackUtils}
 */
public interface ICallback<CONTEXT, RESULT> {
    /**
     * 操作成功完成时的回调方法
     *
     * @param context 回调的上下文对象
     * @param out     结果
     */
    void onFinish(CONTEXT context, RESULT out);

    /**
     * 操作执行失败,抛出异常时的回调方法
     *
     * @param context 回调的上下文对象
     * @param e       异常
     */
    void onException(CONTEXT context, Exception e);
}
