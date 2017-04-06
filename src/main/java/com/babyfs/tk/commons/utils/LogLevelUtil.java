package com.babyfs.tk.commons.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 在运行时修改Logger的日志级别,例如:
 * <code>
 * LogLevelUtil.setSlf4jLevel(NAME, LogLevelUtil.LogLevel.INFO);
 * </code>
 */
public class LogLevelUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogLevelUtil.class);
    private static final Map<LoggerType, LogLevelProvider> LEVEL_PROVIDERS = Maps.newHashMap();

    static {
    }

    private LogLevelUtil() {

    }

    /**
     * 设置指定类型Logger的日志级别
     *
     * @param type  日志的类型
     * @param name  日志的名称
     * @param level 新的日志级别
     * @return true, 成功;false,失败
     * @throws IllegalArgumentException,IllegalStateException
     */
    public static boolean setLevel(final LoggerType type, final String name, final LogLevel level) {
        final LogLevelProvider levelProvider = getLogLevelProvider(type, name);
        Preconditions.checkArgument(level != null, "The level must not be null");
        return levelProvider.setLevel(name, level);
    }

    /**
     * 取得指定类型Logger 的日志级别的String描述
     *
     * @param type 日志的类别
     * @param name 日志的名称
     * @return
     * @throws IllegalArgumentException,IllegalStateException
     */
    public static String getLevelDesc(final LoggerType type, final String name) {
        final LogLevelProvider levelProvider = getLogLevelProvider(type, name);
        return levelProvider.getLevelDesc(name);
    }

    /**
     * @param type
     * @param name
     * @return
     */
    private static LogLevelProvider getLogLevelProvider(LoggerType type, String name) {
        Preconditions.checkArgument(type != null, "The type must not be null");
        Preconditions.checkArgument(name != null, "The name must not be null");
        final LogLevelProvider levelProvider = LEVEL_PROVIDERS.get(type);
        Preconditions.checkState(levelProvider != null, "Can't find the log level provider for %s", type);
        return levelProvider;
    }

    /**
     * @param str
     * @return
     */
    private static String trimOrNull(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 日志的级别,暂时只支持常用的级别
     */
    public static enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * 日志的现实方式
     */
    public static enum LoggerType {
    }

    private static interface LogLevelProvider {
        /**
         * 设置指定名称的Logger 的日志级别
         *
         * @param name  logger 的名称
         * @param level 日志的级别
         * @return true, 修改成功;false,修改失败
         */
        public boolean setLevel(String name, LogLevel level);

        /**
         * 取得指定名称的Logger的日志级别描述
         *
         * @param name
         * @return
         */
        public String getLevelDesc(String name);

        /**
         * @return
         */
        public LoggerType getLoggerType();
    }
}
