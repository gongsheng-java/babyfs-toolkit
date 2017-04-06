package com.babyfs.tk.commons.service.internal;

import com.babyfs.tk.commons.service.annotation.AfterStartStage;

/**
 * {@link AfterStartStage}
 */
public class AfterStartStageRegistry extends StageActionRegistrySupport {
    @Override
    public String toString() {
        return AfterStartStage.class.toString();
    }
}
