package TCP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/*
 * 
 * Modified code from https://gist.github.com/rostyslav
 * 
 * 
 * 
 */

/*
 * 
 *  A class for the client side of the TCP connection
 *  
 * 
 * 
 * Written by Jacob Olsson
 * 
 * 
 */

/*
 * 
 *  A class for the servser side of the TCP connection
 * 
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Network.network_encryption;
import encryption.encryption;

/*
 * 
 *  A class for the client side of the TCP connection
 * 
 */

public class TCPClient {

	public String name;
	private boolean debug = false;
	public Socket socket = null;
	ZIP zip = new ZIP();

	public TCPClient(String name) {
		this.name = name;
	}

	public TCPClient(String name, boolean debug) {
		this.name = name;
		this.debug = debug;
	}

	public void connect(String ipadress, int port) throws UnknownHostException, IOException {
			socket = new Socket(ipadress, port);
	}

	public void connect(InetSocketAddress adress) throws IOException{
			socket = new Socket(adress.getAddress(), adress.getPort());
	}

	public void connect(String ipadress, int port, Runnable run) {
		try {
			socket = new Socket(ipadress, port);
			if (socket.isConnected()) {
				run.run();
			}
		} catch (Exception e) {
			println_err("-Could not connect to TCP server. - " + ipadress + ":" + port);
		}
	}
	
	public void disconnect() throws IOException {
		socket.close();
	}

	public String send(String message, network_encryption encryption) {

		try {

			if (encryption.encryption_status == true) {
				message = encryption.encrypt(message);
			}

			// Send the message to the server
			write_to_socket(message, socket);

			// Get's the return message from the server
			String returnMessage = read_from_socket(socket);

			return returnMessage;

		} catch (Exception e) {
			println_err(e.getMessage());
		}
		return "";
	}

	private String read_from_socket(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		DataInputStream dis = new DataInputStream(in);
		int len = dis.readInt();
		byte[] data = new byte[len];
		if (len > 0) {
			dis.readFully(data);
		}
		return getString(data);
	}

	private void write_to_socket(String message, Socket socket) throws IOException {
		OutputStream out = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		byte[] myByteArray = getBytes(message);
		int len = myByteArray.length;

		dos.writeInt(len);
		if (len > 0) {
			dos.write(myByteArray, 0, len);
		}
	}

	public byte[] getBytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}

	public String getString(byte[] data) {
		return new String(data, Charset.forName("UTF-8"));
	}

	public void send(File file, network_encryption encryption) {
		// : if the file is directory , the file it is first ziped and then sent.
		if (file.isDirectory()) {

			send_directory(file, encryption);

			// : if it is a file it is sent normally.
		} else if (file.isFile()) {
			try {
				send_file(file, encryption);
			} catch (Exception e) {
				println_err("-Client Could not send file");
			}
		}

	}

	public void send_directory(File file, network_encryption encryption) {
		// : if the file is directory , the file it is first ziped and then sent.
		if (file.isDirectory()) {

			File compressedFile = null;
			try {
				compressedFile = zip.compress(file);
				this.send_file(compressedFile, encryption);
			} catch (Exception e) {
				println_err("-Client Could not send directory");
			} finally {
				// : deletes the ziped file.
				compressedFile.delete();

			}

			// : if it is a file it is sent normally.
		} else if (file.isFile()) {
			try {
				send_file(file, encryption);
			} catch (Exception e) {
				println_err("-Client Could not send file");
			}
		}
	}

	private void send_file(File file, network_encryption encryption) {
		String filename = file.getName();
		File to_be_sent = null;
		try {
			// : Encrypts the file if encryption is enabled
			if (encryption.encryption_status == true) {
				to_be_sent = copyFile(file);
				File encryptd = encryption.encrypt_file(to_be_sent, encryption);
				to_be_sent = encryptd;
			} else {
				to_be_sent = file;
			}

			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

			DataOutputStream d = new DataOutputStream(out);
			d.writeUTF(filename);
			Files.copy(to_be_sent.toPath(), d);
			d.close();
			out.close();
		} catch (Exception e) {
			println_err("-Client Could not send File");
		}
		if (encryption.encryption_status == true) {
			deleteFile(to_be_sent);
		}

	}

	private File copyFile(File file) throws IOException {
		String new_fileName = get_freeFileName(file.getName());
		File temp_file = new File(new_fileName);
		Files.copy(file.toPath(), temp_file.toPath());
		return temp_file;
	}

	private File test_file_name(File file) {
		if (file_exists(file.getName())) {
			String new_name = get_freeFileName(file.getName());
			file.renameTo(new File(new_name));
		}
		return file;
	}

	private boolean file_exists(String path) {
		File f = new File(path);
		return f.exists();
	}

	private String get_freeFileName(String fileName) {
		// : name without extension.
		String name = fileName.substring(0, fileName.lastIndexOf('.'));
		String extension = fileName.substring(fileName.lastIndexOf('.'), fileName.length());

		int index = 1;
		while (file_exists(name + "(" + index + ")" + extension)) {
			index++;
		}
		return name + "(" + index + ")" + extension;
	}

	private void deleteFile(File file) {

		if (file.isFile()) {
			boolean couldDelete = file.delete();
			if (!couldDelete) {
				println_err(" Could not delete file " + file.getName());
			}
		} else if (file.isDirectory()) {

			File[] files = file.listFiles();
			for (File seletedFile : files) {
				deleteFile(seletedFile);
			}
			boolean couldDelete = file.delete();
			if (!couldDelete) {
				println_err(" Could not delete folder " + file.getName());
			}
		}

	}

	public Socket getSocket() {
		return this.socket;
	}

	public String getIP() throws IOException {
		return getSocket().getInetAddress().toString();
	}

	public int getport() {
		return this.getSocket().getPort();
	}

	// : Set the debug mode
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	// : prints a error
	private void print_err(String text) {
		if (this.debug) {
			System.err.println("TCP-Client(" + this.name + ")" + text);
		}
	}

	// : print error with a line break
	private void println_err(String text) {
		if (this.debug) {
			System.err.println("TCP-Client(" + this.name + ")" + text);
		}
	}

	// : print message to console
	private void print(String text) {
		if (this.debug) {
			System.out.print("TCP-Client(" + this.name + ")" + text);
		}
	}

	// : print to console with a line break.
	private void println(String text) {
		if (this.debug) {
			System.out.println("TCP-Client(" + this.name + ")" + text);
		}
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

}
