package com.babyfs.tk.commons;

import java.nio.charset.Charset;

/**
 */
public final class Constants {
    /**
     * 系统默认编码:{@value Constants#UTF_8}
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * 系统默认编码
     */
    public static final Charset UTF8_CHARSET = Charset.forName(UTF_8);

    /**
     * 全局配置:服务器的id
     */
    public static final String CONF_SERVER_ID = "server.id";

    /**
     * 禁止
     */
    public static final String DISABLE_START_LIFE_SERVCIE = "disable.life.service";

    private Constants() {

    }
}
