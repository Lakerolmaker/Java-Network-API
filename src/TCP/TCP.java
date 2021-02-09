package TCP;

/*
 * 
 *  A class for holding both the server and the client inside itself.
 *  
 */

public class TCP {

	public TCPClient client = new TCPClient("Client");
	public TCPServer server = new TCPServer("Server");
	
}
