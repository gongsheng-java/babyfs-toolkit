package com.babyfs.tk.galaxy.register.impl;


import com.babyfs.tk.galaxy.register.ServiceServer;

import java.util.List;

public class ServerGroup{
    private List<ServiceServer> list;
    private List<ServiceServer> grayList;

    public ServerGroup(List<ServiceServer> list, List<ServiceServer> grayList) {
        this.list = list;
        this.grayList = grayList;
    }

    public List<ServiceServer> getList() {
        return list;
    }

    public void setList(List<ServiceServer> list) {
        this.list = list;
    }

    public List<ServiceServer> getGrayList() {
        return grayList;
    }

    public void setGrayList(List<ServiceServer> grayList) {
        this.grayList = grayList;
    }
}