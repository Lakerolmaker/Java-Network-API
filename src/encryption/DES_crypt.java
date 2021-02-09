package encryption;

import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 * Modified class made by : Tim Archer 11/11/03
 * @version $Revision: 1.2 $
 */
public class DES_crypt {

    private Cipher encryptCipher = null;
    private Cipher decryptCipher = null;

   
    public DES_crypt(){
    		try {
    			//Generate the secret key
    	        String password = "tSXJwvqTiGZC#K^nt29s10tiyjx1E";
    	        DESKeySpec key;
    			
    			key = new DESKeySpec(password.getBytes());
    		
    	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    	        Key secretKey = keyFactory.generateSecret(key);
    	    	
    	        encryptCipher = Cipher.getInstance("DES");
    	        decryptCipher = Cipher.getInstance("DES");
    	        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
    	        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
    	        
    		} catch (Exception e) {}
    }       
    
    public String encrypt(String input){
    	
    			try {
    				 // Encode the string into bytes using utf-8
        	        byte[] utf8 = input.getBytes("UTF8");
        	        // Encrypt
        	        byte[] enc = encryptCipher.doFinal(utf8);
        	        // Encode bytes to base64 to get a string
        	        return new sun.misc.BASE64Encoder().encode(enc);
        	        
			} catch (Exception e) {
				return null;
			}
       
      }

      public String decrypt(String input){
    	  		try {
    	  			 // Decode base64 to get bytes
    	  	        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(input);
    	  	        byte[] utf8 = decryptCipher.doFinal(dec);
    	  	        // Decode using utf-8
    	  	        return new String(utf8, "UTF8");
    	  	        
			} catch (Exception e) {
				return null;
			}
       
      }
}