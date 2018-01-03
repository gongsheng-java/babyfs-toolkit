
package com.babyfs.tk.galaxy.demo;

import com.babyfs.tk.galaxy.register.DiscoveryProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class DemoDiscoveryProperties implements DiscoveryProperties {

	private static final Log log = LogFactory.getLog(DemoDiscoveryProperties.class);

	private boolean enabled = true;

	private int  SessionTimeOut = 2000000;

	private HostInfo hostInfo = initHostInfo();

	private String ipAddress = this.hostInfo.getIpAddress();

	private String hostname = this.hostInfo.getHostname();

	private boolean preferIpAddress = false;

	private String RegisterUrl = "127.0.0.1:2181";

	private String discoveryPrefix = "/galaxy/discovery";

	private int  connectTimeOut =  1000000;

	private String  port = "8080";

	private String appName = "appName";

	private int ttl = 30;

	private int heartbeatInterval = 25000;

	public String getRegisterUrl() {
		return RegisterUrl;
	}


	public int getConnectTimeOut() {
		return connectTimeOut;
	}


	public int getSessionTimeOut() {
		return SessionTimeOut;
	}


	public String getPort() {
		return port;
	}


	public String getAppName() {
		return appName;
	}


	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	private HostInfo initHostInfo() {
		InetAddress ipAddress = getIpAddress();
		return new HostInfo(ipAddress.getHostAddress(), ipAddress.getHostName());
	}

	public static InetAddress getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> enumNic = NetworkInterface
					.getNetworkInterfaces(); enumNic.hasMoreElements();) {
				NetworkInterface ifc = enumNic.nextElement();
				if (ifc.isUp()) {
					for (Enumeration<InetAddress> enumAddr = ifc
							.getInetAddresses(); enumAddr.hasMoreElements();) {
						InetAddress address = enumAddr.nextElement();
						if (address instanceof Inet4Address
								&& !address.isLoopbackAddress()) {
							return address;
						}
					}
				}
			}
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e){
			return null;
		} catch (IOException e) {
			log.warn("Unable to find non-loopback address", e);
			return null;
		}
	}

	private class HostInfo {
		private final String ipAddress;
		private final String hostname;

		public HostInfo(String ipAddress, String hostname) {
			this.ipAddress = ipAddress;
			this.hostname = hostname;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getHostname() {
			return hostname;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			HostInfo hostInfo = (HostInfo) o;

			if (!ipAddress.equals(hostInfo.ipAddress)) return false;
			return hostname.equals(hostInfo.hostname);
		}

		@Override
		public int hashCode() {
			int result = ipAddress.hashCode();
			result = 31 * result + hostname.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return String.format("HostInfo{ipAddress='%s', hostname='%s'}", ipAddress, hostname);
		}
	}


	public boolean isEnabled() {
		return enabled;
	}


	public String getDiscoveryPrefix() {
		return discoveryPrefix;
	}

	public boolean isPreferIpAddress() {
		return preferIpAddress;
	}



	public int getTtl() {
		return ttl;
	}


	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DemoDiscoveryProperties that = (DemoDiscoveryProperties) o;

		if (enabled != that.enabled) return false;
		if (preferIpAddress != that.preferIpAddress) return false;
		if (ttl != that.ttl) return false;
		if (heartbeatInterval != that.heartbeatInterval) return false;
		if (discoveryPrefix != null ? !discoveryPrefix.equals(that.discoveryPrefix) : that.discoveryPrefix != null)
			return false;
		if (hostInfo != null ? !hostInfo.equals(that.hostInfo) : that.hostInfo != null) return false;
		if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) return false;
		return hostname != null ? hostname.equals(that.hostname) : that.hostname == null;
	}

	@Override
	public int hashCode() {
		int result = (enabled ? 1 : 0);
		result = 31 * result + (discoveryPrefix != null ? discoveryPrefix.hashCode() : 0);
		result = 31 * result + (hostInfo != null ? hostInfo.hashCode() : 0);
		result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
		result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
		result = 31 * result + (preferIpAddress ? 1 : 0);
		result = 31 * result + ttl;
		result = 31 * result + heartbeatInterval;
		return result;
	}

	@Override
	public String toString() {
		return String.format(
				"DemoDiscoveryProperties{enabled=%s, discoveryPrefix='%s', hostInfo=%s, ipAddress='%s', hostname='%s', preferIpAddress=%s, ttl=%d, heartbeatInterval=%d}",
				enabled, discoveryPrefix, hostInfo, ipAddress, hostname, preferIpAddress, ttl, heartbeatInterval);
	}
}
