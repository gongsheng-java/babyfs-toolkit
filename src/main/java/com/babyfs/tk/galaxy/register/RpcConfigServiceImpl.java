package com.babyfs.tk.galaxy.register;


public class RpcConfigServiceImpl implements IRpcConfigService {


    private int port;

    private String appName;

    public RpcConfigServiceImpl(int port, String appName) {
        this.port = port;
        this.appName = appName;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getAppName() {
        return appName;
    }

}
