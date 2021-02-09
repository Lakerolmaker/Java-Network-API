package Network;

import java.net.InetSocketAddress;

public class txt_server {
	
	// : Name of the text server
	private String name;
	// : Hostname of the text server
	private String hostname;
	// : port of the text server
	private int port;

	public txt_server(String name, String hostname, int port) {
		super();
		this.name = name;
		this.hostname = hostname;
		this.port = port;
	}
	
	public txt_server(String name, InetSocketAddress nameServer) {
		super();
		this.name = name;
		this.hostname = nameServer.getAddress().toString().split("/")[1];
		this.port = nameServer.getPort();
	}

	public InetSocketAddress get_adress() {
		return new InetSocketAddress(this.hostname, this.port);
	}

	public String getName() {
		return name;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}	

}
