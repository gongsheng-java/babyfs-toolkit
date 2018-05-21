package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.galaxy.ServicePoint;

import java.util.Map;

/**
 *
 */
public interface IMapService extends ILifeService {
    Map<ServicePoint,Object> getSvrs();
}
