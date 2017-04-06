package com.babyfs.tk.commons;

import java.nio.charset.Charset;

/**
 */
public final class Constants {
    /**
     * 系统默认编码:{@value Constants#DEFAULT_CHARSET}
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 系统默认编码
     */
    public static final Charset DEFAULT_CHARSET_OBJ = Charset.forName(DEFAULT_CHARSET);

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
