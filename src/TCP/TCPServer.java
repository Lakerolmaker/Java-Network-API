package TCP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/*
 * 
 * Modified code from https://gist.github.com/rostyslav
 * 
 * 
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import Network.network_encryption;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/*
 * 
 *  A class for the server side of the TCP connection 
 *
 */

public class TCPServer {

	public String name;
	private boolean debug = false;
	public ServerSocket server = null;
	ZIP zip = new ZIP();

	private File file_server_path = null;

	public TCPServer(String name) {
		this.name = name;
	}

	public TCPServer(String name, boolean debug) {
		this.name = name;
		this.debug = debug;
	}

	public TCPServer(InetSocketAddress adress) {
		this.initializeServer(adress);
	}

	// : finds a free port and then adds a socket to the ip-adress of the system
	public void initializeServer() throws Exception {
		int port = getFreePort();
		InetAddress adress = InetAddress.getLocalHost();
		server = new ServerSocket(port, 10, adress);
	}

	public void initializeServer(InetSocketAddress address) {
		try {
			server = new ServerSocket(address.getPort(), 10, address.getAddress());
		} catch (IOException e) {
			println_err("Could not create server TCP on - " + address.getAddress() + ":" + address.getPort());
		}

	}
	
	public void disconnect() throws IOException {
		server.close();
		server = null;
	}

	public String getIp() {
		return server.getInetAddress().getHostAddress();
	}

	public int getPort() {
		return server.getLocalPort();
	}

	public InetSocketAddress getAdress() {
		return new InetSocketAddress(this.getIp(), this.getPort());
	}

	public void startTextServer(RunnableArg<String> invocation, network_encryption encryption) {

		Runnable serverCode = new Runnable() {

			@Override
			public void run() {

				while (true) {

					try {
						Socket connectionSocket = server.accept();

						String dataString = read_from_socket(connectionSocket);
						if (encryption.encryption_status == true) {
							dataString = encryption.decrypt(dataString);
						}
						invocation.addData(dataString);

						// : Sends a return message
						write_to_socket("ok", connectionSocket);

						// : Executes the server code
						invocation.run();

					} catch (Exception e) {
						println_err(e.getMessage());
					}
				}
			}
		};

		new Thread(serverCode).start();

		//println("Text-server running on - " + this.getIp() + ":" + this.getPort());
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

	public void startFileServer(RunnableArg<File> invocation, network_encryption encryption) {

		Runnable serverCode = new Runnable() {

			@Override
			public void run() {

				while (true) {

					try {
						Socket socket = server.accept();
						File file = saveFile(socket, encryption);

						invocation.addData(file);
						invocation.run();

					} catch (Exception e) {
						println_err(e.getMessage());
					}

				}

			}

		};

		new Thread(serverCode).start();
		println("File-server running on - " + this.getIp() + ":" + this.getPort());

	}

	/**
	 * Set's the path to which the fileserver uses to save files
	 *
	 * @param Folderpath the path to the directory to use
	 * 
	 * @return Nothing
	 */
	public void setFileServerPath(File Folderpath) {
		this.file_server_path = Folderpath;
	}

	// : Saves a file to file when receiving it.
	public File saveFile(Socket socket, network_encryption encryption) throws Exception {
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		String fileName = null;
		File newFile = null;
		try (DataInputStream d = new DataInputStream(in)) {
			fileName = d.readUTF();

			if (this.file_server_path != null) {
				fileName = this.file_server_path.getPath() + File.separator + fileName;
			}

			if (file_exists(fileName)) {
				fileName = get_freeFileName(fileName);
			}

			newFile = new File(fileName);

			// : adds the file to the file system, from the stream
			Files.copy(d, Paths.get(newFile.getPath()));
			d.close();

			if (encryption.encryption_status == true) {
				encryption.decrypt_file(newFile, encryption);
			}

			if (is_zip(newFile)) {
				String file_path = newFile.getPath();
				String folder_path = file_path.substring(0, file_path.lastIndexOf('.'));
				String exstension = file_path.substring(file_path.lastIndexOf('.'), file_path.length());

				File temp = null;
				//: Generates a new folder name if one already exists.
				if (file_exists(folder_path)) {
					String folder_name = get_freeFolderName(folder_path);
					temp = zip.uncompress(newFile, folder_name);
				} else {
					temp = zip.uncompress(newFile);
				}

				newFile.delete();
				return temp;
			}

		} catch (Exception e) {
			println_err(e.getMessage());
		}
		return newFile;
	}

	private static int generateRandomIntIntRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	private boolean file_exists(String path) {
		File f = new File(path);
		return f.exists();
	}

	private boolean file_exists(File f) {
		return f.exists();
	}

	private boolean is_zip(File file) {
		String fileName = file.getName();
		String extension = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
		if (extension.equals(".zip")) {
			return true;
		} else {
			return false;
		}
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

	private String get_freeFolderName(String folderName) {
		// : name without extension.
		int index = 1;
		while (file_exists(folderName + "(" + index + ")")) {
			index++;
		}
		return folderName + "(" + index + ")";
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

	// : Set the debug mode
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	// : prints a error
	private void print_err(String text) {
		if (this.debug) {
			System.err.println("TCP-Server(" + this.name + ")" + text);
		}
	}

	// : print error with a line break
	private void println_err(String text) {
		if (this.debug) {
			System.err.println("TCP-Server(" + this.name + ")" + text);
		}
	}

	// : print message to console
	private void print(String text) {
		if (this.debug) {
			System.out.print("TCP-Server(" + this.name + ")" + text);
		}
	}

	// : print to console with a line break.
	private void println(String text) {
		if (this.debug) {
			System.out.println("TCP-Server(" + this.name + ")" + text);
		}
	}

}
