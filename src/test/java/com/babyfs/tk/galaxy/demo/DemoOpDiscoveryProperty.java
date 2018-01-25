package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.galaxy.register.IRpcConfigService;

public class DemoOpDiscoveryProperty implements IRpcConfigService {


    private int port = 8081;
    private String appName = "op";

    public int getPort() {
        return port;
    }

    public String getAppName() {
        return appName;
    }

}
