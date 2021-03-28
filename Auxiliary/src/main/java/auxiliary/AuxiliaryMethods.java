package auxiliary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
	
	public static void writeLongToChannel (long number, ByteChannel channel) {
		byte[] tempByte = convertLongToByteArray(number);
		ByteBuffer tempBuffer = ByteBuffer.allocate(tempByte.length);
		tempBuffer.put(tempByte);
		tempBuffer.flip();
		try {
			channel.write(tempBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tempBuffer.clear();
	}
	
	public static long readLongFromChannel(ByteChannel channel) {
		ByteBuffer tempBuffer = ByteBuffer.allocate(256);
		try {
			channel.read(tempBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tempBuffer.flip();
		byte[] tempByte = tempBuffer.array();
		long anotherLong = auxiliary.AuxiliaryMethods.convertByteArrayToLong(tempByte);
		return anotherLong;
	}
	
	public static ByteBuffer convertStringToByteBuffer(String string) {
		byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
		ByteBuffer tempBuffer = ByteBuffer.allocate(bytes.length);
		tempBuffer.put(bytes);
		tempBuffer.flip();
		return tempBuffer;
		
	}
	
	public static String readStringFromChannel(ReadableByteChannel channel) {
		ByteBuffer tempBuffer = ByteBuffer.allocate(1024);
		try {
			channel.read(tempBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String string = StandardCharsets.UTF_8.decode(tempBuffer).toString();
		return string;
		
	}
	
}