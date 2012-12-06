package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.util.NWSFile.readHalfWords;

import java.io.DataInputStream;

//import android.util.Log;

public class SymbBlock {
	public int block_len;
	public int layer_num;
	public int layer_len;
		
	public SymbBlock(){
	}
	
	public void readProductBlock(DataInputStream product){
		//int layer_divider;
		//int block_id;
		
		int[] buffer = readHalfWords(product, 1);	// divider

		buffer = readHalfWords(product, 7);
		
		//block_id  = buffer[0];	// Should be 1
		block_len = (buffer[1] << 16) + buffer[2];
		layer_num = buffer[3];
		//layer_divider = buffer[4];	// Should be -1
		layer_len = (buffer[5] << 16) + buffer[6];
		
		//Log.d("Symb block", "Done reading symb block.");		
	}
}
