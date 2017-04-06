package com.babyfs.tk.commons.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogLevelUtilTest {

    public static final String NAME = "test_change_log_level";

    @Test
    public void test_change_log_level() {
        Logger logger = LoggerFactory.getLogger(NAME);
        logger.debug("can't see me");
        logger.info("can see me");
    }
}
