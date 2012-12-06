package me.mattsutter.conditionred.products;

import org.apache.tools.bzip2.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
//import android.util.Log;

/**
 * Class that decompresses and stores the information contained in
 * the symbology block of the radar file.
 * @author Matt Sutter
 *
 */
public class CompressedSymbBlock{
	// Just keeping these around in case I need them.
	
	//public int block_len;
	//public int layer_num;
	//public int layer_len;
	private int symb_block_len;
	private final int product_block_len = 102;
	private final int message_header_len = 18;
		
	public CompressedSymbBlock(){
	}
	
	/**
	 * Reads in the information contained in the symbology block, including the 
	 * radial RLEs.
	 * @param radials - The radial layer to store the RLE data.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	protected void readSymbBlock(DataInputStream product, int message_len, RadialLayer radials) throws InterruptedException, IOException{
		CBZip2InputStream input = setInput(product, message_len);
		
		@SuppressWarnings("unused")
		int[] buffer = new int[4];
		buffer = readHalfWords(input, 1);	// Should be 0xFFFF
		
		buffer = readHalfWords(input, 4);
		
		radials.readRadialLayer(input);
		
		//Log.i("Symb block", "Done reading symb block.");		
	}
	
	/**
	 * Reads in the information contained in the symbology block, including the 
	 * radial RLEs.
	 * @param radials - The radial layer to store the RLE data.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	protected void readSRVSymbBlock(DataInputStream product, int message_len, RadialLayer radials, 
			short increment, float ave_speed, float ave_angle) throws InterruptedException, IOException{
		CBZip2InputStream input = setInput(product, message_len);
		
		@SuppressWarnings("unused")
		int[] buffer = new int[4];
		buffer = readHalfWords(input, 1);	// Should be 0xFFFF
		
		buffer = readHalfWords(input, 4);
		
		radials.readSRVRadialLayer(input, increment, ave_speed, ave_angle);
		
		//Log.i("Symb block", "Done reading symb block.");		
	}
	
	/**
	 * Reads in the bzip2 compressed symbology block and decompresses it.
	 * @param product - 
	 * @param mes_len
	 * @throws IOException 
	 */
	protected CBZip2InputStream setInput(DataInputStream product, int mes_len) throws IOException{
		symb_block_len = mes_len - message_header_len - product_block_len;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(symb_block_len - 2);

		@SuppressWarnings("unused")
		char buffer;
		buffer = (char) product.readUnsignedByte(); // Should be 'B'
		buffer = (char) product.readUnsignedByte(); // Should be 'Z'
		/*if (buffer != 'Z'){
				Log.e("Symb Block", "This file does not contain a valid compressed symb block!");
			}*/

		for (int i=0; i < symb_block_len - 2; i++)
			bytes.write(product.readByte());

		return new CBZip2InputStream(new ByteArrayInputStream(bytes.toByteArray()));
	}

	/**
	 * Reads the specified number of halfwords.
	 * @param data - The DataInputStream you want to read from.
	 * @param halfwords - The number of halfwords (2 bytes) you want to read from the buffer. 
	 * @return Integer array of the values of each halfword.
	 */
	public static int[] readHalfWords(CBZip2InputStream data, int halfwords ){
		int[] buffer = new int[halfwords];
		int one;
		int two;
		
		for (int i=0; i < halfwords; i++){
			one = (char) data.read();
			two = (char) data.read();
			buffer[i] = (one << 8) + two;
		}
		
		return buffer;
	}
}
