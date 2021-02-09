package encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import Network.network_encryption;
import TCP.RunnableArg;
import UDP.UDP;

public class file_encryption {

	public static void main(String[] args) throws Exception {

		network_encryption encryption = new network_encryption();
		encryption.set_encryption(true);
		encryption.set_password("1234567891234567");

		File file = new File(
				"C:\\Users\\jacob\\Google Drive\\Programs\\Java\\Distributed-system\\Network\\schnose pics(1)(1).zip");
		// File encrypted = encrypt_file(file, encryption);
		File decrypted = encryption.decrypt_file(file, encryption);

		System.out.println("done");

	}

	/**
	 * An internal method to encrypt a file
	 *
	 * @param  file the file to be encrypted
	 * @param  encryption the encryption configuration
	 * 
	 * @return The encrypted string
	 * @throws Exception 
	 */
	public File encrypt_file(File file, network_encryption encryption) throws Exception {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] inputBytes = new byte[(int) file.length()];
		inputStream.read(inputBytes);

		inputBytes = encryption.encrypt(inputBytes);
		
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(inputBytes);
		inputStream.close();
		outputStream.close();
		return file;
	}
	
	/**
	 * An internal method to decrypt a file
	 *
	 * @param  file the file to be encrypted
	 * @param  encryption the decryption configuration
	 * 
	 * @return The decrypted string
	 * @throws Exception 
	 */
	public File decrypt_file(File file, network_encryption encryption) throws Exception {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] inputBytes = new byte[(int) file.length()];
		inputStream.read(inputBytes);

		inputBytes = encryption.decrypt(inputBytes);
		
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(inputBytes);
		inputStream.close();
		outputStream.close();
		return file;
	}

}
