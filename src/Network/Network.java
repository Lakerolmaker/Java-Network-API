package Network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dosse.upnp.UPnP;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import TCP.RunnableArg;
import TCP.TCP;
import TCP.TCPClient;
import TCP.TCPServer;
import UDP.UDPClient;
import UDP.UDPServer;
import console_external.consoleFX;
import encryption.AES_crypt;
import jdk.nashorn.internal.parser.JSONParser;
import netscape.javascript.JSObject;
import sun.net.www.http.HttpClient;

/**
 *
 * A Network class that creates a harmonious integration of multiple computers
 * over a closed network.
 * <p>
 * 
 * @version 1.0
 * @author Jacob Olsson ( lakerolmaker@gmail.com )
 */
public class Network {

	public static final String DEFUALT_TCP_TXT_SERVER_NAME = "DEFUALT_TCP_TXT_SERVER";
	public static final String DEFUALT_TCP_NAME_SERVER_NAME = "DEFUALT_TCP_NAME_SERVER";
	public static final String DEFUALT_TCP_PING_SERVER_NAME = "DEFUALT_TCP_PING_SERVER";
	public static final String DEFUALT_TCP_FILE_SERVER_NAME = "DEFUALT_TCP_FILE_SERVER";

	public static final String DEFUALT_UDP_TXT_SERVER_NAME = "DEFUALT_UDP_TXT_SERVER";
	public static final String DEFUALT_UDP_NAME_SERVER_NAME = "DEFUALT_UDP_NAME_SERVER";

	// : The port the network uses when using UDP
	public static final int DEFUALT_UDP_NAME_PORT = 3434;
	public static final int DEFUALT_TCP_NAT_Port = 4167;
	public static final int DEFUALT_UDP_NAT_Port = 4267;


	private ArrayList<NetworkEvent> events = new ArrayList<NetworkEvent>();

	// : Parser library, to parse json string
	private Gson jsonParser = new Gson();
	// : Information about the network that are sent to other nodes are kept here.
	public Node self;
	// : The name of the network
	private String name;
	// : Server that listens for new nodes announcing them self's on the same
	// network.
	private UDPServer nameServer;

	private ArrayList<TCPServer> tcp_txt_servers = new ArrayList<TCPServer>();
	private ArrayList<TCPServer> tcp_file_servers = new ArrayList<TCPServer>();

	private ArrayList<UDPServer> udp_txt_servers = new ArrayList<UDPServer>();

	// : List of all the nodes in the network
	private ArrayList<Node> Nodes = new ArrayList<Node>();

	// : Indicates weather to print to the console
	private boolean debug = false;

	// : indicates weather to use port forwarding.
	private boolean NAT_status = true;

	// : This can be switched on to have different nodes on the same device.
	private Boolean sameDevice = false;

	// : how long the network waits before it updates. number is in seconds
	private long updateFrequency = 10;

	// : number of network instances on the same device
	private int numberOfInstancesOnDevice = 10;

	/**
	 * A collection of functions that govern over the encryption of the system.
	 * <p>
	 * Note: by default the encryption is off.
	 * 
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public network_encryption encryption = new network_encryption();

	/**
	 * Creates a network session.
	 * <p>
	 * note that you will have to connect to the network to use any of the network
	 * features.
	 * <p>
	 * to connect to the nwtork use {@link #connectToNetwork}
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public Network(String name) throws Exception {
		this.name = name;
		this.self = new Node(name);
	}

	/**
	 * Connects to the network by contacting the other nodes
	 * <p>
	 * This function should called when all servers has been created on the current
	 * node.
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void connectToNetwork() throws Exception {
		println("Connecting to the network");
		UPnP.waitInit();

		startTCPNameServer();
		startPingServer();
		setUpdateClock();

		if (NAT_status) {
			addSelfToDB();
			addOtherNodesFromDB();
		} else {
			startUDPNameServer(DEFUALT_UDP_NAME_PORT);
			annoucePresence(DEFUALT_UDP_NAME_PORT);

			// : Wait's for the other nodes to respond.
			sleep(1000);
		}
		println("Connected To Network");
	}

	private void addOtherNodesFromDB() throws Exception {
		PostClass post = new PostClass();
		post.URL = "http://81.230.72.203/api/network/nat.php";
		String response = post.post();

		JsonElement jsonEl = new JsonParser().parse(response);
		JsonArray jsonArr = jsonEl.getAsJsonArray();
		Thread thread = new Thread() {

			public void run() {
				for (int i = 0; i < jsonArr.size(); i++) {

					JsonObject obj = jsonArr.get(i).getAsJsonObject();
					int id = obj.get("id").getAsInt();

					String data = obj.get("data").getAsString();
					Node node = jsonParser.fromJson(data, Node.class);
					node.setId(id);

					if (addNode(node)) {

						txt_server name_server = node.getTCPTxtServer(DEFUALT_TCP_NAME_SERVER_NAME);
						try {
							println("Conneting to node: " + node.getName());
							sendSelfData(name_server);

							println("Pinging node:" + node.getName());
							long ping = ping(node.getTCPTxtServer(DEFUALT_TCP_PING_SERVER_NAME));

							for (NetworkEvent ev : events)
								ev.onUserAdd(node, ping);
						} catch (IOException e) {
							println_err("Could not connect to node : " + node.getName());
						}
					}

				}

			}
		};
		thread.start();
	}

	/**
	 * Enables the use of mutible nodes on the same computer
	 * <p>
	 * By default the number of nodes on the network is 10
	 * <p>
	 * To change this use {@link #setInstancesOnSameDevice}
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void setSameDeive(Boolean sameDevice) {
		this.sameDevice = sameDevice;
	}

	/**
	 * set's the number of nodes that are on this machine
	 * 
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson
	 */
	public void setInstancesOnSameDevice(int number) {
		this.numberOfInstancesOnDevice = number;
	}
	
	public void setName(String name) {
		this.name = name;
		this.self.setName(name);;
	}

	/**
	 * Set's the debug mode of the network. This determines if the network should
	 * print to the console.
	 * <p>
	 * Note: by default this set to false.
	 * 
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Get's the name of the network
	 * 
	 * @return the name of the network
	 * @author Jacob Olsson
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set's the mode of the network to either local mode, or external mode.
	 * <p>
	 * To either use the network on a closed network, or to use on multiple network
	 * with the help of port forwarding.
	 * 
	 * @return the name of the network
	 * @author Jacob Olsson
	 */
	public void setNAT(boolean NAT_status) {
		this.NAT_status = NAT_status;
	}

	private void setUpdateClock() {
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {

				for (Node node : Nodes) {
					Thread thread = new Thread() {
						public void run() {

							try {
								long ping = ping(node.getTCPTxtServer(DEFUALT_TCP_PING_SERVER_NAME));

								for (NetworkEvent ev : events)
									ev.onPingUpdate(node, ping);
							} catch (Exception e) {
								Nodes.remove(node);
								for (NetworkEvent ev : events)
									ev.onUserLeave(node);
							}

						}
					};
					thread.start();
				}

			}
		}, this.updateFrequency, this.updateFrequency, TimeUnit.SECONDS);
	}

	public long ping(txt_server server) throws UnknownHostException, IOException {
		TCPClient client = new TCPClient("client");
		long startTime = System.currentTimeMillis();
		client.connect(server.getHostname(), server.getPort());
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		return duration;
	}

	/**
	 * Opens a TCP port to the Internet.
	 * 
	 * @param port the port to be opened
	 * 
	 * @return Nothing
	 * @throws Exception
	 */
	private int openTcpPort(int port) throws Exception {

		if (UPnP.isUPnPAvailable()) {
			if (UPnP.isMappedTCP(port)) {
				println_err("UPnP port forwarding not enabled: port is already mapped. Trying another port");
				return openTcpPort(port + 1);
			} else if (UPnP.openPortTCP(port)) {
				println("UPnP port forwarding enabled on port : " + port);
				return port;
			} else {
				println_err("UPnP port forwarding failed");
				throw new Exception("UPnP port forwarding failed");
			}
		} else {
			println_err("UPnP is not available");
			throw new Exception("UPnP is not available");
		}

	}

	/**
	 * closes a TCP port to the Internet.
	 * 
	 * @param port the port to be closed
	 * 
	 * @return Nothing
	 */
	private boolean closeTcpPort(int port) {
		return UPnP.closePortTCP(port);
	}

	/**
	 * Opens a UDP port to the internet.
	 * 
	 * @param port the port to be opened
	 * 
	 * @return Nothing
	 */
	private int openUdpPort(int port) {

		if (UPnP.isUPnPAvailable()) {
			if (UPnP.isMappedUDP(port)) {
				println_err("UPnP port forwarding not enabled: port is already mapped. Trying another port");
				return openUdpPort(port + 1);
			} else if (UPnP.openPortUDP(port)) {
				println("UPnP port forwarding enabled on port : " + port);
				return port;
			} else {
				println_err("UPnP port forwarding failed");
				return 0;
			}
		} else {
			println_err("UPnP is not available");
			return 0;
		}

	}

	public String getExternalIP() throws Exception {
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

		return in.readLine(); // you get the IP as a String
	}

	public String getLocalIP() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	private void removeSelfFromDB() throws Exception {
		PostClass post = new PostClass();
		post.URL = "http://81.230.72.203/api/network/remove_user.php";
		post.addPostParamter("id", String.valueOf(self.getId()));
		String response = post.post();
	}

	private void addSelfToDB() throws Exception {
		PostClass post = new PostClass();
		post.URL = "http://81.230.72.203/api/network/add_user.php";
		String data = jsonParser.toJson(self);
		post.addPostParamter("data", data);
		String response = post.post();
		self.setId(Integer.valueOf(response));
	}

	/**
	 * disconnect from the network by closing the connection
	 * <p>
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void disconnectFromNetwork() throws Exception {

		if (NAT_status) {
			removeSelfFromDB();
		}

		for (TCPServer server : tcp_txt_servers) {
			if (NAT_status) {
				UPnP.closePortTCP(server.getPort());
				
			}
			server.disconnect();
		}

		for (UDPServer server : udp_txt_servers) {
			if (NAT_status) {
				UPnP.closePortUDP(server.getPort());
			}
			server.disconnect();
		}
	}

	/**
	 * Starts a text server on the network. This server is of a generic variety and
	 * does not have a name. NOTE: There can only be one generic text server per
	 * network.
	 * <p>
	 * Sending a message without name to a node will make it end up in this one.
	 *
	 * @param name       the name of the text server. this is used when sending
	 *                   messages to it from another node.
	 * @param invocation the code that will be run when the server receives a new
	 *                   message
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 * @throws Exception
	 */
	public void startTextServer(RunnableArg<String> invocation) throws Exception {
		this.startTextServer(DEFUALT_TCP_TXT_SERVER_NAME, invocation);
	}

	/**
	 * Starts a text server on the network.
	 * <p>
	 * This server has a assigned name and can be specifically sent to from another
	 * node.
	 *
	 * @param name       the name of the text server. this is used when sending
	 *                   messages to it from another node.
	 * @param invocation the code that will be run when the server receives a new
	 *                   message
	 * 
	 * @return Nothing
	 * @throws Exception
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void startTextServer(String name, RunnableArg<String> invocation) throws Exception {
		// : Creates a new tcp text-server
		TCPServer server = new TCPServer(name);
		server.setDebug(this.debug);

		if (this.NAT_status) {
			server.initializeServer(new InetSocketAddress(getLocalIP(), openTcpPort(DEFUALT_TCP_NAT_Port)));
		} else {
			server.initializeServer();
		}

		server.startTextServer(invocation, encryption);
		this.tcp_txt_servers.add(server);

		// : creates a new record of a text server on this node
		txt_server serv_info;
		if (this.NAT_status) {
			serv_info = new txt_server(name, getExternalIP(), server.getPort());
		} else {
			serv_info = new txt_server(name, getLocalIP(), server.getPort());
		}
		this.self.addTCPTxtServer(serv_info);
	}

	public void startTextServer_UDP(RunnableArg<String> invocation) throws Exception {
		this.startTextServer_UDP(DEFUALT_UDP_TXT_SERVER_NAME, invocation);
	}

	public void startTextServer_UDP(String name, RunnableArg<String> invocation) throws Exception {
		// : Creates a new tcp text-server
		UDPServer server = new UDPServer(name);
		server.setDebug(this.debug);

		int port = server.getFreePort();

		if (this.NAT_status) {
			port = openUdpPort(port);
		}

		server.start(port, invocation, encryption);

		this.udp_txt_servers.add(server);

		// : creates a new record of a text server on this node
		txt_server serv_info;
		if (this.NAT_status) {
			serv_info = new txt_server(name, getExternalIP(), port);
		} else {
			serv_info = new txt_server(name, getLocalIP(), port);
		}
		this.self.addUDPTxtServer(serv_info);
	}

	public void startByteServer_UDP(String name, int bufferSize, RunnableArg<byte[]> invocation) throws Exception {
		// : Creates a new tcp text-server
		UDPServer server = new UDPServer(name);
		server.setDebug(this.debug);

		int port;

		if (this.NAT_status) {
			port = openUdpPort(DEFUALT_UDP_NAT_Port);
		} else {
			port = server.getFreePort();
		}

		server.start(port, bufferSize, invocation, encryption);

		this.udp_txt_servers.add(server);

		// : creates a new record of a text server on this node
		txt_server serv_info;
		if (this.NAT_status) {
			serv_info = new txt_server(name, getExternalIP(), port);
		} else {
			serv_info = new txt_server(name, getLocalIP(), port);
		}
		this.self.addUDPTxtServer(serv_info);
	}

	/**
	 * Starts a file server on the network.This server is of a generic variety and
	 * does not have a name. NOTE: There can only be one generic text server per
	 * network.
	 *
	 * @param invocation the code that will be run when the server receives a new
	 *                   message
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 * @throws Exception
	 */
	public void startFileServer(RunnableArg<File> invocation) throws Exception {
		this.startFileServer(DEFUALT_TCP_FILE_SERVER_NAME, invocation);
	}

	/**
	 * Starts a file server on the network.
	 * <p>
	 * This server has a assigned name and can be specifically sent to from another
	 * node
	 *
	 * @param name       the name of the file server. this is used when sending
	 *                   messages to it from another node.
	 * @param invocation the code that will be run when the server receives a new
	 *                   message
	 * 
	 * @return Nothing
	 * @throws Exception
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void startFileServer(String name, RunnableArg<File> invocation) throws Exception {
		// : Creates a new tcp text-server
		TCPServer server = new TCPServer(name);
		server.setDebug(this.debug);

		if (this.NAT_status) {
			server.initializeServer(new InetSocketAddress(getExternalIP(), openTcpPort(DEFUALT_TCP_NAT_Port)));
		} else {
			server.initializeServer();
		}

		server.startFileServer(invocation, encryption);
		this.tcp_file_servers.add(server);

		// : creates a new record of a text server on this node
		file_server serv_info = new file_server(name, server.getAdress());
		this.self.addFileServer(serv_info);
	}

	/**
	 * Starts a file server on the network.
	 * <p>
	 * This server has a assigned name and can be specifically sent to from another
	 * node
	 *
	 * @param name       the name of the file server. this is used when sending
	 *                   messages to it from another node.
	 * @param invocation the code that will be run when the server receives a new
	 *                   message
	 * @param serverPath the path the server will use when saving a file
	 * 
	 * @return Nothing
	 * @throws Exception
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void startFileServer(String name, File serverPath, RunnableArg<File> invocation) throws Exception {
		// : Creates a new tcp text-server
		TCPServer server = new TCPServer(name);
		server.setDebug(this.debug);
		server.setFileServerPath(serverPath);

		if (this.NAT_status) {
			server.initializeServer(new InetSocketAddress(getExternalIP(), openTcpPort(DEFUALT_TCP_NAT_Port)));
		} else {
			server.initializeServer();
		}

		server.startFileServer(invocation, encryption);
		this.tcp_file_servers.add(server);

		// : creates a new record of a text server on this node
		file_server serv_info = new file_server(name, server.getAdress());
		this.self.addFileServer(serv_info);
	}

	/**
	 * Sends a message to the generic message server of a node
	 *
	 * @param node the node who'm you want to send to
	 * @param msg  the message to be sent
	 * 
	 * 
	 * @return Nothing
	 */
	public void sendToNode(Node node, String msg) throws Exception {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		tcpClient.connect(node.getTCPTxtServer(DEFUALT_TCP_TXT_SERVER_NAME).get_adress());
		tcpClient.send(msg, encryption);
	}

	/**
	 * Sends a message to a specific text server of a node
	 *
	 * @param node      the node who'm you want to send to
	 * @param serveName the name of the text server on the node
	 * @param msg       the message to be sent
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 * 
	 */
	public void sendToNode(Node node, String serverName, String msg) throws Exception {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		tcpClient.connect(node.getTCPTxtServer(serverName).get_adress());
		tcpClient.send(msg, encryption);
	}

	/**
	 * Sends a message to all nodes that match the provided name
	 *
	 * @param nodeName the name of the nodes, to whom you want to send a message
	 * 
	 * @param msg      the message to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToNodes(String nodeName, String msg) throws Exception {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : getNodes(nodeName)) {
			tcpClient.connect(node.getTCPTxtServer(DEFUALT_TCP_TXT_SERVER_NAME).get_adress());
			tcpClient.send(msg, encryption);
		}
	}

	/**
	 * Sends a message to all nodes that match the provided name
	 * <p>
	 * This message is sent to the specific text server that is provided
	 * (ServerName)
	 *
	 * @param nodeName   the name of the nodes, to whom you want to send a message
	 * @param ServerName the name of the text server on the nodes
	 * @param msg        the message to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToNodes(String nodeName, String ServerName, String msg) throws Exception {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : getNodes(nodeName)) {
			txt_server server = node.getTCPTxtServer(ServerName);
			tcpClient.connect(server.get_adress());
			tcpClient.send(msg, encryption);
		}
	}

	/**
	 * Sends a message to all nodes, using a new TCP connection.
	 * <p>
	 * this message will be received by the generic message server
	 * 
	 * @param msg the message to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToAllNodes(String msg) {
		for (Node node : Nodes) {
			TCPClient tcpClient = new TCPClient("client");
			try {
				txt_server server = node.getTCPTxtServer(DEFUALT_TCP_TXT_SERVER_NAME);
				tcpClient.connect(server.get_adress());
				tcpClient.send(msg, encryption);
			} catch (IOException e) {

			}
		}
	}

	public void sendToAllNodes_UDP(String msg) {
		for (Node node : Nodes) {
			UDPClient udpClient = new UDPClient("client");
			try {
				InetAddress adress = InetAddress
						.getByName(node.getUDPTxtServer(DEFUALT_UDP_TXT_SERVER_NAME).getHostname());
				int port = node.getUDPTxtServer(DEFUALT_UDP_TXT_SERVER_NAME).getPort();
				udpClient.broadcast_withoutReply(msg, adress, port, encryption);
			} catch (IOException e) {

			}
		}
	}

	public void sendToNodes_UDP(String serverName, byte[] msg) {
		UDPClient udpClient = new UDPClient("client");
		for (Node node : Nodes) {
			try {
				udpClient.broadcast_withoutReply(msg,
						InetAddress.getByName(node.getUDPTxtServer(serverName).getHostname()),
						node.getUDPTxtServer(serverName).getPort(), encryption);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void sendToNodes_UDP(Node node, String serverName, byte[] msg) {
		UDPClient udpClient = new UDPClient("client");

		try {
			udpClient.broadcast_withoutReply(msg, InetAddress.getByName(node.getUDPTxtServer(serverName).getHostname()),
					node.getUDPTxtServer(serverName).getPort(), encryption);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Sends a message to all nodes
	 * <p>
	 * This message is sent to the specific text server that is provided
	 * (ServerName)
	 * 
	 * @param msg        the message to be sent
	 * @param serverName the name of the text server on the nodes
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToAllNodes(String msg, String serverName) throws Exception {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : Nodes) {
			tcpClient.connect(node.getTCPTxtServer(serverName).get_adress());
			tcpClient.send(msg, encryption);
		}
	}

	public void sendToAllNodes_UDP(String msg, String serverName) throws Exception {
		for (Node node : Nodes) {
			UDPClient udpClient = new UDPClient("client");
			try {
				InetAddress adress = InetAddress.getByName(node.getUDPTxtServer(serverName).getHostname());
				int port = node.getUDPTxtServer(serverName).getPort();
				udpClient.broadcast_withoutReply(msg, adress, port, encryption);
			} catch (IOException e) {

			}
		}
	}

	/**
	 * Sends a file to all nodes that match the provided name
	 * <p>
	 * This file is sent to the generic text server
	 *
	 * @param nodeName the name of the nodes, to whom you want to send a message
	 * @param file     the file to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToNodes(String nodeName, File file) {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : getNodes(nodeName)) {
			try {
				tcpClient.connect(node.getFileServer(DEFUALT_TCP_FILE_SERVER_NAME).get_adress());
			} catch (IOException e) {
			}
			tcpClient.send(file, encryption);
		}
	}

	/**
	 * Sends a file to all nodes that match the provided name
	 * <p>
	 * This file is sent to the specific text server that is provided (ServerName)
	 *
	 * @param nodeName   the name of the nodes, to whom you want to send a message
	 * @param ServerName the name of the file server on the nodes
	 * @param file       the file to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToNodes(String nodeName, String ServeName, File file) {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : getNodes(nodeName)) {
			try {
				tcpClient.connect(node.getFileServer(ServeName).get_adress());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tcpClient.send(file, encryption);
		}
	}

	/**
	 * Sends a file to all nodes
	 *
	 * 
	 * @param file the file to be sent
	 * 
	 * 
	 * @return Nothing
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public void sendToAllNodes(File file) {
		TCPClient tcpClient = new TCPClient("client");
		tcpClient.setDebug(this.debug);
		for (Node node : Nodes) {
			try {
				tcpClient.connect(node.getFileServer(DEFUALT_TCP_FILE_SERVER_NAME).get_adress());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tcpClient.send(file, encryption);
		}
	}

	public void addListener(NetworkEvent toAdd) {
		events.add(toAdd);
	}

	/**
	 * return a list of all nodes on the network
	 *
	 * @return A list of all the nodes in the network
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public ArrayList<Node> getAllNodes() {
		return this.Nodes;
	}

	/**
	 * returns the name server
	 *
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public InetSocketAddress getNameServer() {
		return self.getTCPTxtServer(DEFUALT_TCP_NAME_SERVER_NAME).get_adress();
	}

	public InetSocketAddress getTxtServer() {
		return self.getTCPTxtServer(DEFUALT_TCP_TXT_SERVER_NAME).get_adress();
	}

	public InetSocketAddress getFileServer() {
		return self.getTCPTxtServer(DEFUALT_TCP_FILE_SERVER_NAME).get_adress();
	}

	/**
	 * return a list of all nodes that match the specified name on the network
	 *
	 * @author Jacob Olsson ( lakerolmaker@gmail.com )
	 */
	public ArrayList<Node> getNodes(String name) {
		ArrayList<Node> retrived_nodes = new ArrayList<Node>();
		for (Node node : Nodes) {
			if (node.getName().equals(name)) {
				retrived_nodes.add(node);
			}
		}
		return retrived_nodes;
	}

	// : Adds a node to the list of nodes, if the node isn't in the list
	private boolean addNode(Node newnode) {
		if (same_node(self, newnode)) {
			return false;
		}
		for (Node node : Nodes) {
			if (same_node(node, newnode)) {
				return false;
			}
		}
		Nodes.add(newnode);
		printNode(newnode);

		return true;
	}

	private boolean same_node(Node node1, Node node2) {
		return node1.toString().equals(node2.toString());
	}

	private void printNode(Node node) {
		println("Node info recived: " + node.toString());
	}

	// : Sends out a broadcast, of the address of the name server to the network to
	// announce yourself
	private void annoucePresence(int port) throws UnknownHostException, IOException {
		UDPClient udpClient = new UDPClient("client");
		udpClient.setDebug(this.debug);
		txt_server server_data = new txt_server(this.name, getNameServer());
		String data = jsonParser.toJson(server_data);
		if (this.sameDevice) {
			for (int i = 0; i < this.numberOfInstancesOnDevice; i++) {

				udpClient.broadcast_withoutReply(data, InetAddress.getByName("255.255.255.255"), port, encryption);
				port++;
			}
		} else {
			udpClient.broadcast(jsonParser.toJson(this.self), InetAddress.getByName("255.255.255.255"), port,
					encryption);
		}
	}

	// : Listens for nodes sharing their information
	private void startTCPNameServer() throws Exception {

		this.startTextServer(DEFUALT_TCP_NAME_SERVER_NAME, new RunnableArg<String>() {
			@Override
			public void run() {
				Node node = jsonParser.fromJson(this.getData(), Node.class);
				if (addNode(node)) {
					txt_server name_serve = new txt_server("reply",
							node.getTCPTxtServer(DEFUALT_TCP_NAME_SERVER_NAME).get_adress());
					try {
						sendSelfData(name_serve);

						long ping = ping(node.getTCPTxtServer(DEFUALT_TCP_PING_SERVER_NAME));

						for (NetworkEvent ev : events)
							ev.onUserAdd(node, ping);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void startPingServer() throws Exception {
		this.startTextServer(DEFUALT_TCP_PING_SERVER_NAME, new RunnableArg<String>() {
			@Override
			public void run() {

			}
		});
	}

	// : Listens for other nodes announcing themselves.
	// : If a node announces themselves,a message is sent with the information of
	// ourself
	private void startUDPNameServer(int port) {
		try {
			nameServer = new UDPServer(name);
			nameServer.setDebug(debug);
			nameServer.start(port, new RunnableArg<String>() {
				@Override
				public void run() {
					String json = this.getData();
					txt_server server = jsonParser.fromJson(json, txt_server.class);
					try {
						sendSelfData(server);
					} catch (IOException e) {

					}
				}
			}, encryption);
		} catch (SocketException e) {
			// : Is the port is already in use and the same device is on
			if ((e.getMessage().contains("Address already in use")) && (this.sameDevice)) {
				println_err("Port " + port + " allready in use, testing next one");
				startUDPNameServer(port + 1);
			} else {
				println_err(e.getMessage());
			}
		}
	}

	private void sendSelfData(txt_server server) throws IOException {
		TCPClient client = new TCPClient("client");
		client.connect(server.getHostname(), server.getPort());
		client.send(jsonParser.toJson(self), encryption);
		client.disconnect();
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	// : prints a error
	private void print_err(String text) {
		if (this.debug) {
			System.err.print("Network(" + this.name + ")-" + text);
		}
	}

	// : print error with a line break
	private void println_err(String text) {
		if (this.debug) {
			System.err.println("Network(" + this.name + ")-" + text);
		}
	}

	// : print message to console
	private void print(String text) {
		if (this.debug) {
			System.out.print("Network(" + this.name + ")-" + text);
		}
	}

	// : print to console with a line break.
	private void println(String text) {
		if (this.debug) {
			System.out.println("Network(" + this.name + ")-" + text);
		}
	}

}
