package Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import encryption.file_encryption;
import encryption.AES_crypt;

public class network_encryption {

	public boolean encryption_status = false;
	private AES_crypt AES = new AES_crypt();
	private file_encryption file_encryption = new file_encryption();

	/**
	 * Set's the state of the encryption on the network.
	 * <p>
	 * This reflects weather to have encryption on or off
	 *
	 * @param value a boolean of the state of the network encryption
	 * 
	 * @return Nothing
	 */
	public void set_encryption(boolean value) {
		encryption_status = value;
	}

	/**
	 * Set's the password of the encryption of the network
	 * <p>
	 * NOTE: This must be the same on all devices in the system
	 *  <p>
	 * Note: The password must be 16 characters long
	 * 
	 * @param password The password to be used
	 * 
	 * @return Nothing
	 * @throws Exception 
	 */
	public void set_password(String password) throws Exception {
		if(password.length() == 16) {
			AES.key = password;
		}else {
			throw new Exception("Password is not 16 characters long.");
		}
		
	}

	/**
	 * An internal method to encrypt the traffic in the network
	 *
	 * @param value The string to be encrypted
	 * 
	 * @return The encrypted string
	 * @throws Exception
	 */
	public String encrypt(String value) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return AES.encrypt(value);
	}

	/**
	 * An internal method to encrypt the traffic in the network
	 *
	 * @param value The base64 array to be encrypted
	 * 
	 * @return The encrypted string
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] value) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return AES.encrypt(value);
	}

	/**
	 * An internal method to decrypt the traffic in the network
	 *
	 * @param value The string to be decrypted
	 * 
	 * @return The decrypted string
	 * @throws Exception
	 */
	public String decrypt(String value) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return AES.decrypt(value);
	}

	/**
	 * An internal method to decrypt the traffic in the network
	 *
	 * @param value base64 array to be decrypted
	 * 
	 * @return The decrypted string
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] value) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return AES.decrypt(value);
	}

	/**
	 * An internal method to encrypt a file
	 *
	 * @param file       the file to be encrypted
	 * @param encryption the encryption configuration
	 * 
	 * @return The encrypted string
	 * @throws Exception
	 */
	public File encrypt_file(File file, network_encryption encryption) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return file_encryption.encrypt_file(file, encryption);
	}

	/**
	 * An internal method to decrypt a file
	 *
	 * @param file       the file to be encrypted
	 * @param encryption the decryption configuration
	 * 
	 * @return The decrypted string
	 * @throws Exception
	 */
	public File decrypt_file(File file, network_encryption encryption) throws Exception {
		if (AES.key == null) {
			throw new Exception("No password Given");
		}
		return file_encryption.decrypt_file(file, encryption);
	}

}
