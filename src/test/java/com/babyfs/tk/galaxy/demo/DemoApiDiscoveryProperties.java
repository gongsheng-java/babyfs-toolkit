
package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.galaxy.register.IRpcConfigService;

public class DemoApiDiscoveryProperties implements IRpcConfigService {

	private int port = 8080;

	private String appName = "api";

	public int getPort() {
		return port;
	}
	public String getAppName() {
		return appName;
	}



}
