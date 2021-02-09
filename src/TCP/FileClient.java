package TCP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/*
 * 
 *  A class for sending a file over TCP
 *  
 * 
 * 
 */

public class FileClient {

	private Socket s;

	public FileClient(String host, int port) {
		try {
			s = new Socket(host, port);
			sendFile(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(Socket sr) throws IOException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		try {
			File myFile = new File("D:\\Download\\duy_is_a_goddamn_idiot.zip");
			byte b[] = new byte[(int) myFile.length()];
			fis = new FileInputStream(myFile);
			bis = new BufferedInputStream(fis);
			bis.read(b, 0, b.length);
			os = sr.getOutputStream();
			os.write(b, 0, b.length);
			os.flush();
		} finally {
			if (bis != null)
				bis.close();
			if (os != null)
				os.close();
			if (sr != null)
				sr.close();
		}
	}

	public static void main(String[] args) {
		FileClient fc = new FileClient("localhost", 1988);
	}

}