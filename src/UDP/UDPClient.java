package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

import com.sun.org.apache.xml.internal.security.Init;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import Network.network_encryption;

public class UDPClient {
	
	private String name;
	private boolean debug = false;
	
	public UDPClient(String name) {
		this.name = name;
		Init.init();
	}

	private DatagramSocket socket = null;

	// : Broadcast a message to all nodes in the network.
	// : Returns the reply from the server
	public String broadcast(String broadcastMessage, InetAddress address, int port, network_encryption encryption) {
		try {
			
			socket = new DatagramSocket();
			socket.setBroadcast(true);

			if(encryption.encryption_status == true) {
				broadcastMessage = encryption.encrypt(broadcastMessage);
			}
			
			byte[] buffer = getBytes(broadcastMessage);

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
			socket.send(packet);
			

			DatagramPacket replyData = new DatagramPacket(new byte[256], 256);

			socket.receive(replyData);

			String replyMessage = getMessage(replyData);

			// : Closes the socket connection
			socket.close();

			// : Returns the message from the server
			return replyMessage;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void broadcast_withoutReply(String broadcastMessage, InetAddress address, int port, network_encryption encryption) {
		try {
			
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			
			if(encryption.encryption_status == true) {
				broadcastMessage = encryption.encrypt(broadcastMessage);
			}
			
			byte[] buffer = getBytes(broadcastMessage);
			
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
			socket.send(packet);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void broadcast_withoutReply(byte[] broadcastMessage, InetAddress address, int port, network_encryption encryption) {
		try {
			
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			
			if(encryption.encryption_status == true) {
				broadcastMessage = encryption.encrypt(broadcastMessage);
			}
						
			DatagramPacket packet = new DatagramPacket(broadcastMessage, broadcastMessage.length, address, port);
			socket.send(packet);

		} catch (Exception e) {
			
		}
	}
	
	public byte[] getBytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}
	
	public String getString(byte[] data) {
		return new String(data, Charset.forName("UTF-8"));
	}
	
	public byte[] getB64(String str) throws Base64DecodingException {
		return Base64.decode(str.getBytes());
	}
	
	public String B64_toString(byte[] data) throws Base64DecodingException {
		return new String(Base64.encode(data));
	}

	// get's the message inside the packet
	private String getMessage(DatagramPacket data) {
		return new String(data.getData()).trim();
	}
	
	//: Set the debug mode
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	//: prints a error
	private void print_err(String text) {
		if (this.debug) {
			System.err.println(text);
		}
	}

	//: print error with a line break
	private void println_err(String text) {
		if (this.debug) {
			System.err.println(text);
		}
	}

	//: print message to console
	private void print(String text) {
		if (this.debug) {
			System.out.print(text);
		}
	}

	//: print to console with a line break.
	private void println(String text) {
		if (this.debug) {
			System.out.println(text);
		}
	}

}
