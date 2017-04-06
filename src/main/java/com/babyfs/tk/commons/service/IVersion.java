package com.babyfs.tk.commons.service;

import com.google.common.collect.ImmutableList;
import com.babyfs.tk.commons.base.Pair;

/**
 * 版本接口
 */
public interface IVersion {
    /**
     * 取得所有的jar(jar中带有git.properties)的版本信息
     *
     * @return
     */
    ImmutableList<Pair<String, String>> getAllVersions();

}
