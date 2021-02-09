package Network;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Node {

	private ArrayList<txt_server> tcp_txt_servers = new ArrayList<txt_server>();
	private ArrayList<txt_server> udp_txt_servers = new ArrayList<txt_server>();
	private ArrayList<file_server> file_servers = new ArrayList<file_server>();
	
	private int id;

	private String name;

	public Node(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return "NODE(" + this.name + ")";
	}

	public void addTCPTxtServer(txt_server server) {
		tcp_txt_servers.add(server);
	}

	public void addUDPTxtServer(txt_server server) {
		udp_txt_servers.add(server);
	}

	public void addFileServer(file_server server) {
		file_servers.add(server);
	}

	public txt_server getTCPTxtServer(String serverName) {
		for (txt_server server : tcp_txt_servers) {
			if (server.getName().equals(serverName)) {
				return server;
			}
		}
		return null;
	}

	public txt_server getUDPTxtServer(String serverName) {
		for (txt_server server : udp_txt_servers) {
			if (server.getName().equals(serverName)) {
				return server;
			}
		}
		return null;
	}

	public file_server getFileServer(String name) {
		for (file_server server : file_servers) {
			if (server.getName().equals(name)) {
				return server;
			}
		}
		return null;
	}

	public ArrayList<txt_server> getAllTCPTxtServer() {
		return this.tcp_txt_servers;
	}

	public ArrayList<txt_server> getAllUDPTxtServer() {
		return this.udp_txt_servers;
	}

	public ArrayList<file_server> getAllFileServer() {
		return this.file_servers;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	

}
