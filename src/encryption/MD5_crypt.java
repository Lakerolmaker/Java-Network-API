package encryption;
import java.security.MessageDigest;
public class MD5_crypt {
	
		private static String Salt = "APAZ0l!*0lv%#wnevn6^C8H*i6G2M";
	
	    public static String encrypt(String input){
	   
	    		try {
				
		    		String toBeEnctrypted = input + Salt;
			    	
		    		String encrypted = "";
		    		
		        MessageDigest md = MessageDigest.getInstance("MD5");
		        md.update(toBeEnctrypted.getBytes());
		        
		        byte byteData[] = md.digest();
		       
		        //: Converts it into hex
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < byteData.length; i++) {
		         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		        }

		        encrypted = sb.toString();
		        
		       return encrypted;
		       
	    		} catch (Exception e) {
				return null;
			}
	        
	    }
	
	    public static void setSalt(String input) {
	    		Salt = input;
	    }
	    
		public static String MD5(String md5) {
			   try {
			        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			        byte[] array = md.digest(md5.getBytes());
			        StringBuffer sb = new StringBuffer();
			        for (int i = 0; i < array.length; ++i) {
			          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			       }
			        return sb.toString();
			    } catch (java.security.NoSuchAlgorithmException e) {
			    }
			    return null;
		}
}
