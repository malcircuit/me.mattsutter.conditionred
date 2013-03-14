package me.mattsutter.conditionred.graphics;

import static me.mattsutter.conditionred.products.RadarProduct.MAX_SCAN_NUM;
import static me.mattsutter.conditionred.products.RadarProduct.MIN_SCAN_NUM;
import android.util.Log;
import me.mattsutter.conditionred.products.RadarProduct;

public class Animation{
	private static final int INVALID_SEQ = -1;
	
	private final RadarProduct[] array;
	private final int[] indices;
	private final int frame_num;
	
	private int current_frame = INVALID_SEQ;
	public int anim_start = INVALID_SEQ;
	
	
	public Animation(int frame_num){
		this.frame_num = frame_num;
		array = new RadarProduct[frame_num];
		indices = new int[frame_num];
		
		reset();
	}
	
	public int addFrame(RadarProduct product){
		final int index = frameIndex(product.prod_block.vol_scan_num);
		insert(product, index);
		return index;
	}
	
	public void updateAnimStart(int vol_scan_num){
		if (anim_start < MIN_SCAN_NUM){
			anim_start = vol_scan_num;
			Log.d("Animation", "Starting Scan Num changed: " + Integer.toString(anim_start));
			return;
		}
		
		final int anim_end = (anim_start - frame_num + MAX_SCAN_NUM) % MAX_SCAN_NUM;
		assert anim_start != anim_end;

		if (anim_start > anim_end){
			if (vol_scan_num > anim_start){
				final int diff = vol_scan_num - anim_start;
				anim_start = vol_scan_num;

				Log.d("Animation", "Starting Scan Num changed: " + Integer.toString(anim_start));
				shift(0, diff);
			}
			else if (vol_scan_num < anim_end){
				final int diff = MAX_SCAN_NUM - anim_start + vol_scan_num - MIN_SCAN_NUM;
				anim_start = vol_scan_num;

				Log.d("Animation", "Starting Scan Num changed: " + Integer.toString(anim_start));
				shift(0, diff);
			}
		}
		else if (anim_start < anim_end && (vol_scan_num < anim_end && vol_scan_num > anim_start)){
			final int diff = vol_scan_num - anim_start;
			anim_start = vol_scan_num;

			Log.d("Animation", "Starting Scan Num changed: " + Integer.toString(anim_start));
			shift(0, diff);
		}
		
	}
	
	private int frameIndex(int scan_num){
		final int index = (anim_start - scan_num + MAX_SCAN_NUM) % MAX_SCAN_NUM;
		Log.d("Animation", "Scan Num:" + Integer.toString(scan_num) + " Frame Index: " + Integer.toString(index));
		return index;
	}
	
	public RadarProduct getCurrentFrame(){
		if (current_frame >= 0)
			return array[indices[current_frame]];
		return null;
	}
	
	public void reset(){
		anim_start = INVALID_SEQ;
		for (int i = 0; i < frame_num; i++){
			indices[i] = INVALID_SEQ;
			array[i] = null;
		}
		
		current_frame = INVALID_SEQ;
	}
	
	private void insert(RadarProduct product, int frame_index){		
		if (indices[frame_index] != INVALID_SEQ)
			shift(frame_index, 1);
		
		indices[frame_index] = insertAtOpenIndex(product);
	}
	
	private int insertAtOpenIndex(RadarProduct product){
		int index = INVALID_SEQ;
		for (int i = 0; i < frame_num; i++)
			if (array[i] == null){
				index = i;
				array[i] = product;
				break;
			}
		
		return index;
	}
	
	private void shift(int start_index, int displacement){			
		for (int i = frame_num - displacement; i < frame_num; i++)
			if (indices[i] > 0)
				array[indices[i]] = null;
		
		for (int i = frame_num - 1; i >= start_index + displacement; i--)
			indices[i] = indices[i - displacement];
		
		for (int i = start_index; i < start_index + displacement; i++)
			indices[i] = INVALID_SEQ;
	}
	
	public int nextFrame(){		
		int index = INVALID_SEQ;

		if (current_frame == INVALID_SEQ){
			for (int i = 0; i < frame_num; i++){
				if (indices[i] >= 0){
					index = i;
					break;
				}
			}
			
			current_frame = index;
			return current_frame;
		}
		else{
			for (int i = 1; i < frame_num; i++){
				if (indices[(current_frame + i) % frame_num] >= 0){
					index = (current_frame + i) % frame_num;
					break;
				}
			}
			
			if (index >= 0)
				current_frame = index;
			
			return current_frame;
			
		}
	}
	
	public int previousFrame(){		
		int index = INVALID_SEQ;

		if (current_frame == INVALID_SEQ){
			for (int i = frame_num - 1; i >= 0; i--){
				if (indices[i] >= 0){
					index = i;
					break;
				}
			}
			
			current_frame = index;
			return current_frame;
		}
		else{
			for (int i = 1; i < frame_num; i++){
				if (indices[(current_frame - i + frame_num) % frame_num] >= 0){
					index = (current_frame - i + frame_num) % frame_num;
					break;
				}
			}
			
			if (index >= 0)
				current_frame = index;
			
			return current_frame;
			
		}
	}
}
