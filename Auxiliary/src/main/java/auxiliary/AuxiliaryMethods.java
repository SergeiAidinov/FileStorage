package auxiliary;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.primitives.Longs;


public class AuxiliaryMethods {

	public static String handleInputFromTextArea(String string) {
		string.trim();
		Pattern pattern = Pattern.compile("\n");
		Matcher matcher = pattern.matcher(string);
		string = matcher.replaceAll("");
		return string;
	}

	public static byte[] convertLongToByteArray(long bytesToTransmit) {
		byte[] bytes = Longs.toByteArray(bytesToTransmit);
		return bytes;
		
	}
	
	public static long convertByteArrayToLong(byte[] bytes) {
		long result = Longs.fromByteArray(bytes);
		return result;
		
	}
}
