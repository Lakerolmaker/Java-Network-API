package UDP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import Network.network_encryption;
import TCP.RunnableArg;

public class UDPServer {

	private String name;
	private boolean debug = false;
	private DatagramSocket socket;
	private int port;
	

	public UDPServer(String name) {
		this.name = name;
	}

	public UDPServer(String name, boolean debug) {
		this.name = name;
		this.debug = debug;
	}

	public void start(int port, RunnableArg<String> invocation, network_encryption encryption) throws SocketException {

		socket = new DatagramSocket(port);
		DatagramPacket packet = new DatagramPacket(new byte[256], 256);
		this.port = port;
		Runnable serverCode = new Runnable() {
			@Override
			public void run() {

				while (true) {

					try {
						socket.receive(packet);
						String dataString;

						if (encryption.encryption_status == true) {
							byte[] buffer = new byte[packet.getLength()];
							System.arraycopy(packet.getData(), packet.getOffset(), buffer, 0, packet.getLength());
							dataString = getString(buffer);
							dataString = encryption.decrypt(dataString);
						} else {
							dataString = getString(packet.getData()).trim();
						}

						// : Adds the message to the invocation queue
						invocation.addData(dataString);

						InetAddress address = packet.getAddress();
						int port = packet.getPort();
					

						// : Sends a reply back to the client
						String reply = "ok";
						socket.send(getPacket(reply, address, port));

						// : Calls the server code
						invocation.run();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

		};

		new Thread(serverCode).start();
		println("UDP(" + this.name + ") text-server running on port - " + port);

	}
	
	public void start(int port, int bufferSize, RunnableArg<byte[]> invocation, network_encryption encryption) throws SocketException {

		socket = new DatagramSocket(port);
		DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
		this.port = port;
		Runnable serverCode = new Runnable() {
			@Override
			public void run() {

				while (true) {
					
					try {
						socket.receive(packet);
						byte[] data;

						if (encryption.encryption_status == true) {
							data = packet.getData();
							data = encryption.decrypt(data);
						} else {
							data = packet.getData();
						}

						// : Adds the message to the invocation queue
						invocation.addData(data);

						InetAddress address = packet.getAddress();
						int port = packet.getPort();

						// : Sends a reply back to the client
						String reply = "ok";
						socket.send(getPacket(reply, address, port));

						// : Calls the server code
						invocation.run();
					} catch (Exception e) {
					}

				}

			}

		};

		new Thread(serverCode).start();
		println("UDP(" + this.name + ") text-server running on port - " + port);

	}
	
	public void disconnect() {
		this.socket.close();
	}

	public byte[] getB64(String str) throws Base64DecodingException {
		return Base64.decode(str.getBytes());
	}

	public String B64_toString(byte[] data) throws Base64DecodingException {
		return new String(Base64.encode(data));
	}

	public byte[] getBytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

	public String getString(byte[] data) {
		return new String(data, Charset.forName("UTF-8"));
	}

	// get's the message inside the packet
	private String getMessage(DatagramPacket data) {
		return new String(data.getData()).trim();
	}

	// : Creates a packet from a message, address and port
	private DatagramPacket getPacket(String message, InetAddress address, int port) {
		byte[] byteSendMessage = message.getBytes();
		return new DatagramPacket(byteSendMessage, byteSendMessage.length, address, port);
	}
	
	public int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();
			try {
				socket.close();
			} catch (IOException e) {
				// Ignore IOException on close()
			}
			return port;
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					println_err(e.getMessage());
				}
			}
		}
		throw new IllegalStateException("Could not find a free TCP/IP port");
	}
	
	public int getPort() {
		return this.port;
	}

	// : Set the debug mode
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	// : prints a error
	private void print_err(String text) {
		if (this.debug) {
			System.err.println(text);
		}
	}

	// : print error with a line break
	private void println_err(String text) {
		if (this.debug) {
			System.err.println(text);
		}
	}

	// : print message to console
	private void print(String text) {
		if (this.debug) {
			System.out.print(text);
		}
	}

	// : print to console with a line break.
	private void println(String text) {
		if (this.debug) {
			System.out.println(text);
		}
	}

}
