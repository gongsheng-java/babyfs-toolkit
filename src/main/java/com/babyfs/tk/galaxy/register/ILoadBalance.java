package com.babyfs.tk.galaxy.register;


public interface ILoadBalance {

    ServiceInstance getServerByAppName(String appName);
}
