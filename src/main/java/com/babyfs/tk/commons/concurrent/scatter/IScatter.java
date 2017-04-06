package com.babyfs.tk.commons.concurrent.scatter;

import java.util.concurrent.Callable;

/**
 * 定义计算任务的接口
 *
 * @param <OUTPUT> 结果的类型
 */
public interface IScatter<OUTPUT> extends Callable<OUTPUT> {
    OUTPUT call();
}


