package Network;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;

import TCP.RunnableArg;
import TCP.TCPServer;

public class Network_Test_Local_TCP {

	public static void main(String[] args) throws Exception {
		File server_save_path = new File("server save files");

		Network network1 = new Network("client1");
		network1.setDebug(true);
		network1.setNAT(false);
		network1.setSameDeive(true);
		network1.setInstancesOnSameDevice(10);
		network1.encryption.set_encryption(true);
		network1.encryption.set_password("1234567891234567");
		network1.startTextServer(new RunnableArg<String>() {

			@Override
			public void run() {
				System.out.println("Recived on generic tcp-text-server : " + this.getData());

			}

		});
		network1.startTextServer("messages", new RunnableArg<String>() {

			@Override
			public void run() {
				System.out.println("Recived on specific tcp-text-server: " + this.getData());

			}

		});
		network1.startFileServer("files", server_save_path, new RunnableArg<File>() {

			@Override
			public void run() {
				if (this.getData().isDirectory()) {
					System.out.println("Recived Directory : " + this.getData().getName());
				} else {
					System.out.println("Recived File : " + this.getData().getName());
				}
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
		network2.addListener(new NetworkEvent() {

			@Override
			public void onUserAdd(Node node, long ping) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onUserLeave(Node node) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPingUpdate(Node node, long ping) {
				System.out.println("Ping to node( " + node.getName() + ") " + ping + "ms");
		
			}
		});
		network2.connectToNetwork();

		URL res1 = Network_Test_Local_TCP.class.getClassLoader().getResource("test files/test photo.jpg");
		File file1 = Paths.get(res1.toURI()).toFile();

		URL res2 = Network_Test_Local_TCP.class.getClassLoader().getResource("test files/folder with photos");
		File file2 = Paths.get(res2.toURI()).toFile();

		network2.sendToNodes("client1", "files", file1);
		network2.sendToNodes("client1", "files", file2);
		network2.sendToAllNodes("Test message to all nodes");
		network2.sendToNodes("client1", "messages", "Test message to specific node");
		

		
	}

}
