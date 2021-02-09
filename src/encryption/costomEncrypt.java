package encryption;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class costomEncrypt {

	public static String oneOf(String input) throws UnsupportedEncodingException {
		
		
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(input);

		int size = byteBuffer.limit();
		int multiplier = 1;
		int addition = 1;
		
		byte[] encoded = new byte[size];
		
		for(int i = 0; i < size; i++) {
			
			int value = byteBuffer.get(i);
			
			int change1 = value + addition;
			
			encoded[i] = (byte)change1;
			
		}
	
		String output = new String(encoded, "UTF-8");

		return output;
		
	}
	
	
}
