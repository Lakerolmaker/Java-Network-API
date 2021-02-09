package encryption;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class B64_crypt {
	

	public static String encrypt(String input) {

		try {
			 return Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
		} catch (Exception e) {
			return null;
		}		
	}
	
		public static String decrypt(String input) {
			try {
				byte[] decodedArr = Base64.getDecoder().decode(input);
				String decodedText = new String(decodedArr, "UTF-8");
			return decodedText;
			} catch (Exception e) {
				return null;
			}
	}

}
