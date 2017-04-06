package com.babyfs.tk.commons.config.internal;

import com.google.inject.Inject;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.config.IGlobalService;

/**
 *
 */
public class GlobalServcieImpl implements IGlobalService {
    @Inject
    protected IConfigService globalConfigServcie;

    @Override
    public IConfigService getGlobalConfigService() {
        return globalConfigServcie;
    }
}
