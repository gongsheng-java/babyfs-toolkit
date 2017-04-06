package com.babyfs.tk.commons.service;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.GitVersionUtil;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打印jar包的版本信息
 */
public class VersionModule extends ServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionModule.class);

    @Override
    protected void configure() {
        printVersions();
    }

    private void printVersions() {
        ImmutableList<Pair<String, String>> allVersions = GitVersionUtil.getAllVersions();
        if (allVersions == null) {
            return;
        }
        for (Pair<String, String> version : allVersions) {
            LOGGER.info("Version of {} [{}]", version.getFirst(), version.getSecond());
        }
    }

    public static final class VersionImpl implements IVersion {
        @Override
        public ImmutableList<Pair<String, String>> getAllVersions() {
            return GitVersionUtil.getAllVersions();
        }
    }
}
