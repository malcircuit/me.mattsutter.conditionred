package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.util.NWSFile.readHalfWords;
import static me.mattsutter.conditionred.products.CompressedSymbBlock.readHalfWords;

import java.io.DataInputStream;

import me.mattsutter.conditionred.util.Unsigned;

import org.apache.tools.bzip2.CBZip2InputStream;

/** 
 * Class that holds all of the radials.
 * @author Matt Sutter
 *
 */
public class RadialLayer{
	// Some of these aren't useful, or redundant.  
	// I just commented them out so I don't forget they are there.
	
	//private int range_bin_index;
	public int range_bin_num;
	//private int center_x;
	//private int center_y;
	//private int range_scale_factor;
	public int num_radials;
	//protected Radial[] radials;
	
	public byte[][] rle;
//	public boolean[][] bin_is_drawn;
	public int[] start_angle;
	public float[] angle_delta;
	
	/**
	 * Constructor.
	 */
	public RadialLayer(){
	}
	
	/**
	 * Parses information from the radar product about the actual radial image, Legacy version.
	 * @param product - The product's byte stream you want to read from.
	 * @throws InterruptedException 
	 */
	protected void readRadialLayer(DataInputStream product) throws InterruptedException{
		int[] buffer = readHalfWords(product, 1);	// Should be 0xAF1F
		buffer = readHalfWords(product, 6);
		
		// Read in values
		//range_bin_index = buffer[0];
		range_bin_num = buffer[1];
		//center_x = buffer[2];
		//center_y = buffer[3];
		//pxl_per_bin = buffer[4];
		num_radials = buffer[5];

		rle = new byte[num_radials][range_bin_num];
//		bin_is_drawn = new boolean[num_radials][range_bin_num];
		start_angle = new int[num_radials];
		angle_delta = new float[num_radials];
				
		// Read in the radials
		for (int i=0; i < num_radials; i++){
			if (Thread.currentThread().isInterrupted()){
				Thread.currentThread().interrupt();
				throw new InterruptedException();
			}
			int[] loop_buffer;
			int rleIndex = 0;

			// Read in the data values
			buffer = readHalfWords(product, 3);
			int rle_half_words = buffer[0];
			

			int angle = (int)(((float)buffer[1])/10.0f + 0.5f) - 90;
			
			if (angle < 0)
				angle += num_radials;

			start_angle[angle] = angle;
			angle_delta[angle] = ((float)buffer[2])/10.0f;

//			for (int n = 0; n < range_bin_num; n++){
//				bin_is_drawn[angle][n] = false;
//			}

			for (int j=0; j < rle_half_words; j++){
				// Read in the RLE values
				loop_buffer = readHalfWords(product, 1);
				int num_bins1 = 0, value1 = 0, num_bins2 = 0, value2 = 0;

				loop_buffer = splitWord(loop_buffer[0]);
				num_bins1 = loop_buffer[0];
				value1 = loop_buffer[1];
				num_bins2 = loop_buffer[2];
				value2 = loop_buffer[3];

				for ( int n = rleIndex; n < ( num_bins1 + rleIndex ); n++){
					rle[angle][n] = 0;
					Unsigned.putUnsignedByte(rle[angle], value1, n);
				}
				rleIndex += num_bins1;
				for ( int n = rleIndex; n < ( num_bins2 + rleIndex ); n++){
					rle[angle][n] = 0;
					Unsigned.putUnsignedByte(rle[angle], value2, n);
				}
				rleIndex += num_bins2;
			}
			
			if ((int)(angle_delta[angle] + 0.5f) >= 2){
				if (angle == num_radials - 1){
					int index = 0;
					for(int j=0; j < (int)(angle_delta[angle] + 0.5f) - 1; j++){
						angle_delta[0 + j] = angle_delta[angle + j] - 1.0f;
						rle[0 + j] = rle[angle + j];

//						for (int n = 0; n < range_bin_num; n++){
//							bin_is_drawn[0 + j][n] = false;
//						}
						index++;
					}
					
					i += index;
					
					angle_delta[angle] = 1.0f;
					continue;
				}
				else{
					angle_delta[angle + 1] = angle_delta[angle] - 1.0f;
					angle_delta[angle] = 1.0f;
					rle[angle + 1] = rle[angle];

//					for (int n = 0; n < range_bin_num; n++){
//						bin_is_drawn[angle + 1][n] = false;
//					}
					
					i++;
					continue;
				}
			}
		}
	}
	
	/**
	 * Parses information from the radar product about the actual radial image.
	 * @param product - The product's byte stream you want to read from.
	 * @throws InterruptedException 
	 */
	protected void readRadialLayer(CBZip2InputStream symb_block) throws InterruptedException{
		int[] buffer = new int[7];
		
		buffer = readHalfWords(symb_block, 1);	// Should be 0xFFFF
		buffer = readHalfWords(symb_block, 2);	// Byte length
		buffer = readHalfWords(symb_block, 7);
		
		// Read in values
		//packet_code = buffer[0];	//  Should be 16
		//range_bin_index = buffer[1]; // Nominally this should be 0
		range_bin_num = buffer[2];
		//center_x = buffer[3];
		//center_y = buffer[4];
		//range_scale_factor = buffer[5];
		num_radials = buffer[6];

		rle = new byte[num_radials][range_bin_num];
//		bin_is_drawn = new boolean[num_radials][range_bin_num];
		start_angle = new int[num_radials];
		angle_delta = new float[num_radials];

		int angle;
		int rle_bytes;
		// Read in the radials
		for (int i=0; i < num_radials ; i++){
			if (Thread.currentThread().isInterrupted()){
				Thread.currentThread().interrupt();
				throw new InterruptedException();
			}
			// Read in the data values
			buffer = readHalfWords(symb_block, 3);
			rle_bytes = buffer[0];
			
			// The unit circle for Android is 90 degrees off from the one the NWS uses.
			angle = (int)(((float) buffer[1])/10.0f + 0.5f) - 90;
			
			if (angle < 0){
				start_angle[angle + num_radials] = angle + num_radials;
				angle_delta[angle + num_radials] = ((float)buffer[2]) / 10.0f;
				for (int n = 0; n < range_bin_num; n++){
//					bin_is_drawn[angle + num_radials][n] = false;
					Unsigned.putUnsignedByte(rle[angle + num_radials], symb_block.read(), n);
				}
				// Sometimes the number of range bins can be an odd number, but the number of bytes is always an even number.
				// So, the number of bytes can be more than the number of bins, which totally fucks everything up if 
				// we don't read that extra byte.
				if (rle_bytes - range_bin_num == 1)
					symb_block.read();
			}
			else{
				start_angle[angle] = angle + num_radials;
				angle_delta[angle] = ((float)buffer[2]) / 10.0f;
				for (int n = 0; n < range_bin_num; n++){
//					bin_is_drawn[angle][n] = false;
					Unsigned.putUnsignedByte(rle[angle], symb_block.read(), n) ;
				}
				// Sometimes the number of range bins can be an odd number, but the number of bytes is always an even number.
				// So, the number of bytes can be more than the number of bins, which totally fucks everything up if 
				// we don't read that extra byte.
				if (rle_bytes - range_bin_num == 1)
					symb_block.read();
			}	
		}
	}
	
	/**
	 * Parses information from the radar product about the actual radial image.
	 * @param product - The product's byte stream you want to read from.
	 * @throws InterruptedException 
	 */
	protected void readSRVRadialLayer(CBZip2InputStream symb_block, short increment, 
			float ave_speed, float ave_angle) throws InterruptedException{
		int[] buffer = new int[7];
		
		buffer = readHalfWords(symb_block, 1);	// Should be 0xFFFF
		//layer_len = (buffer[1] << 16) + buffer[2]
		buffer = readHalfWords(symb_block, 2);
		
		buffer = readHalfWords(symb_block, 7);

		// Read in values
		//range_bin_index = buffer[1];
		range_bin_num = buffer[2];
		//center_x = buffer[3];
		//center_y = buffer[4];
		//range_scale_factor = buffer[5];
		num_radials = buffer[6];

		rle = new byte[num_radials][range_bin_num];
//		bin_is_drawn = new boolean[num_radials][range_bin_num];
		start_angle = new int[num_radials];
		angle_delta = new float[num_radials];
		
		int angle;
		
		ave_angle = ave_angle - 90.0f;
		
		if (ave_angle < 0)
			ave_angle += (float)num_radials;
		
		int rle_bytes;
		// Read in the radials
		for (int i=0; i < num_radials ; i++){	
			if (Thread.currentThread().isInterrupted()){
				Thread.currentThread().interrupt();
				throw new InterruptedException();
			}
			// Read in the data values
			buffer = readHalfWords(symb_block, 3);
			rle_bytes = buffer[0];
			
			float inc = ((float)increment)/10.0f;
			int correction_factor;
			angle = (int)(((float)buffer[1])/10.0f + 0.5f) - 90 ;

			
			if (angle < 0)
				angle += num_radials;
			
			if ((float)angle >= ave_angle)
				correction_factor = (int) (( (ave_speed * Math.cos(Math.toRadians((double)((float)angle - ave_angle)))) / inc) + 0.5f);
			else
				correction_factor = (int) (( (ave_speed * Math.cos(Math.toRadians((double) Math.abs((float)angle - ave_angle)))) / inc) + 0.5f);

			int temp;
			start_angle[angle] = angle;
			angle_delta[angle] = ((float)buffer[2])/10.0f;

			for (int n = 0; n < range_bin_num; n++){
//				bin_is_drawn[angle][n] = false;

				temp = symb_block.read();
				if (!(temp == 1 || temp == 0))
					if (temp + correction_factor == 1)
						rle[angle][n] = 0;
					else
						Unsigned.putUnsignedByte(rle[angle], temp + correction_factor, n);
				else
					Unsigned.putUnsignedByte(rle[angle], temp, n);

				if (rle[angle][n] > 255)
					Unsigned.putUnsignedByte(rle[angle], 255, n);
				if (rle[angle][n] < 0)
					rle[angle][n] = 0;
			}
			// Sometimes the number of range bins can be an odd number, but the number of bytes is always an even number.
			// So, the number of bytes can be more than the number of bins, which totally fucks everything up if 
			// we don't read that extra byte.
			if (rle_bytes - range_bin_num == 1)
				symb_block.read();
		}
	}
	
	/**
	 * This reads the product file and parses the necessary information from it
	 * @param product - The product's byte stream to read from.
	 * @param _range_bins - 
	 */
	protected void readRLE(DataInputStream product){
	//	range_bins = _range_bins;
		//bin_is_drawn = new boolean [range_bins];
		
		
	}
		
	/**
	 * Helper function to get one specific set of range bins out of the radial layer.
	 * @param index - The radial you want.
	 * @return Array of the color values of the radial.
	 */
	/*protected int[] getRLE(int index){
		int[] out = new int[range_bin_num];
		
		for (int n=0; n < range_bin_num; n++){
			out[n] = radials[index].rle[n];
		}
		
		return out;
	}*/
	
	/**
	 * Helper function to get the number of range bins for each radial in the layer.
	 * @return Number of range bins for this radial.
	 */
	protected int getNumberOfBins(){
		return range_bin_num;
	}
	
	/*protected void redraw(Canvas canvas, float pxls_per_bin, Point center){
		for ( Radial radial : radials){
			radial.redraw(canvas, pxls_per_bin, center);
		}
	}*/
	
	/** Takes one word and splits it into 4, 4 bit chunks. 
	 * 
	 * @param word - The 16 bits you want to split up.
	 * @return Array of the the values of the 4 half-byte chunks.
	 */
	private int[] splitWord(int word){
		int[] chunks = new int[4];
		int firstByte = 0;
		int secondByte = 0;
		firstByte = word >> 8;
		secondByte = word - ( firstByte << 8 );

		chunks[0] = firstByte >> 4;
		chunks[1] = firstByte - ( chunks[0] << 4 );
		chunks[2]= secondByte >> 4;
		chunks[3] = secondByte - ( chunks[2] << 4 );
		
		return chunks;
	}
}