package me.mattsutter.conditionred.products;

import java.io.DataInputStream;
import java.io.IOException;

import static me.mattsutter.conditionred.util.NWSFile.readHalfWords;

//import android.util.Log;

public class MessageHeader {
	// In bytes
	public static final int OFFSET = 30;
	public static final int LENGTH = 9;
	
	public final String wmo_header;
//	public final int message_code;
//	public final int message_date;
//	public final int message_time;
	public final int mes_len;
//	public final int source_id;
//	public final int dest_id;
	public final int num_blocks;
	
	public MessageHeader(DataInputStream product){
		wmo_header = String.copyValueOf(readChars(product, OFFSET/2));
		int[] buffer = readHalfWords(product, LENGTH);
//		message_code = buffer[0];
//		message_date = buffer[1];
//		message_time = (buffer[2] << 16) + buffer[3];
		mes_len = (buffer[4] << 16) + buffer[5];
//		source_id = buffer[6];
//		dest_id = buffer[7];
		num_blocks = buffer[8];
	}
	
	/**
	 * Reads the specified number of characters (in 2 byte chunks).
	 * @param read_buf - BufferedReader you want to read from.
	 * @param chars - The number of chars you want to read from the buffer. 
	 * @return A character array.
	 */
	private static char[] readChars(DataInputStream read_buf, int chars){
		char[] buffer = new char[chars * 2];
		//int one;
		//int two;
		
		for (int i = 0; i < chars * 2; i += 2){
			try{
				buffer[i] = (char) read_buf.readByte();
				buffer[i + 1] = (char) read_buf.readByte();
			}
			catch (IOException e){
				//Log.e("readWords()", "Read failed", e);
			}
		}
		
		return buffer;
	}
}
