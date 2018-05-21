package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.galaxy.ServicePoint;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public class MapTest extends LifeServiceSupport implements IMapTest {
    private final Map<ServicePoint, Object> svrs;
    private final ExecutorService be;


    @Inject
    public MapTest(@Named("ok") Map<ServicePoint, Object> svrs, @Named("back") ExecutorService be) {
        this.svrs = svrs;
        this.be = be;
    }

    @Override
    public Map<ServicePoint, Object> getSvrs() {
        return svrs;
    }
}
