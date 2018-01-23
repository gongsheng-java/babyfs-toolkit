package com.babyfs.tk.galaxy.register;


public class RpcConfigServiceImpl implements IRpcConfigService {


    private int port;

    private String appName;

    private String urlPrefix;

    public RpcConfigServiceImpl(int port, String appName, String urlPrefix) {

        this.port = port;
        this.appName = appName;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getUrlPrefix() {
        return urlPrefix;
    }
}
