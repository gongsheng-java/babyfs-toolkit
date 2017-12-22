package com.babyfs.tk.galaxy.register;

import java.util.List;

public interface IRule {

    ServiceInstance choose(List<ServiceInstance> list);

}
