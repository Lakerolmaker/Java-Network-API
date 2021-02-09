package Network;

import com.dosse.upnp.UPnP;

public class remove_UPnP {
	
	static int start_port = 4160;

	public static void main(String[] args) {
		for (int i = 0; i < 6000; i++) {
			int port = start_port + i;
			System.out.println("Trying to close TCP-port : " + port + " status : " + UPnP.closePortTCP(port));
			System.out.println("Trying to close UDP-port : " + port + " status : " + UPnP.closePortUDP(port));
		}
		

	}

}
