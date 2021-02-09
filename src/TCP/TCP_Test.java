package TCP;

import java.io.File;

import Network.network_encryption;

public class TCP_Test {
	
	public static void main(String[] args) throws Exception {

		network_encryption encryption =  new network_encryption();

		
		TCP tcp = new TCP();	
		tcp.server.initializeServer();
		tcp.server.startTextServer(new RunnableArg<String>() {

			@Override
			public void run() {
				System.out.println("Recived : " + this.getData());
			}
		},encryption);
		
	
		tcp.client.connect(tcp.server.getAdress());
		String returned_msg = tcp.client.send("yo" , encryption);
		System.out.println("Returned : " + returned_msg);
		
		TCP tcp_file = new TCP();
		tcp_file.server.initializeServer();
		tcp_file.server.startFileServer(new RunnableArg<File>() {

			@Override
			public void run() {
				System.out.println("Recived file : " + this.getData().getName());
				
			}
		},encryption);
		
		File file = new File("/Users/jacobolsson/Desktop/empty.txt");
		tcp_file.client.connect(tcp_file.server.getAdress());
		tcp_file.client.send(file, encryption);
	}
}
