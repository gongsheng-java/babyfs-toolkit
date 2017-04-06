package com.babyfs.tk.service.basic;

import java.util.Properties;

/**
 * Something that may be configured with a {@link Properties}.
 * <p/>
 */
public interface IConfigurable {
    /**
     * Set the configuration to be used by this object.
     */
    void setConf(Properties conf);

    void afterPropertySet();
}
