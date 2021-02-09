package UDP;

import java.net.InetAddress;

import Network.network_encryption;
import TCP.RunnableArg;

public class UDP_Test {

	public static void main(String[] args) throws Exception {
	
		network_encryption encryption =  new network_encryption();
		encryption.set_encryption(true);
		encryption.set_password("1234567891234567");

		UDP udp = new UDP();
		udp.server.start(5400, new  RunnableArg<String>() {
			@Override
			public void run() {
				System.out.println("Recived message : " + this.getData());
			}
			
		},encryption);
		

		String reply = udp.client.broadcast("hello", InetAddress.getByName("255.255.255.255") , 5400,encryption);
		System.out.println("Reply from server : " + reply);
	}
	
	
}
