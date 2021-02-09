package Network;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;

import TCP.RunnableArg;
import TCP.TCPServer;

public class Network_Test_Local_UDP {

	public static void main(String[] args) throws Exception {
		File server_save_path = new File("server save files");

		Network network1 = new Network("client1");
		network1.setDebug(true);
		network1.setNAT(false);
		network1.setSameDeive(true);
		network1.setInstancesOnSameDevice(10);
		network1.encryption.set_encryption(true);
		network1.encryption.set_password("1234567891234567");
		network1.startTextServer_UDP(new RunnableArg<String>() {

			@Override
			public void run() {
				System.out.println("Recived on generic udp-text-server: " + this.getData());

			}
		});
		network1.startTextServer_UDP("messages", new RunnableArg<String>() {

			@Override
			public void run() {
				System.out.println("Recived on specific udp-text-server: " + this.getData());

			}
		});
		network1.connectToNetwork();

		Network network2 = new Network("client2");
		network2.setDebug(true);
		network2.setNAT(false);
		network2.setSameDeive(true);
		network2.setInstancesOnSameDevice(10);
		network2.encryption.set_encryption(true);
		network2.encryption.set_password("1234567891234567");
		network2.connectToNetwork();
		network2.sendToAllNodes_UDP("hello");
		network2.sendToAllNodes_UDP("hello", "messages");
	}

}
