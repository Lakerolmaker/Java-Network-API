package Network;

import java.net.InetSocketAddress;

public class file_server {

	// : Name of the file server
	private String name;
	// : Hostname of the file server
	private String hostname;
	// : port of the file server
	private int port;

	public file_server(String name, String hostname, int port) {
		super();
		this.name = name;
		this.hostname = hostname;
		this.port = port;
	}
	
	public file_server(String name, InetSocketAddress adress) {
		super();
		this.name = name;
		this.hostname = adress.getHostName();
		this.port = adress.getPort();
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
