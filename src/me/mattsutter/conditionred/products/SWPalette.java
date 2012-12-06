package me.mattsutter.conditionred.products;
/**
 * Applies to products 32 (Digital Hybrid Scan Reflectivity), 94 (Digital Base Reflectivity), 
 * 153 (Super Res Base Reflectivity), 194 (Digital Base Reflectivity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class SWPalette implements Palette{

	//private int step;
	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	//private int RF = -1;
	private int color_num = 0;
	//private static final PAL_TYPES type = Palette.PAL_TYPES.REFL;
	private float min_value;
	private float max_value;
	private float increment;
	//private int num_levels;
	//private static final int OPAQUE = 0xFF000000;
	
	public SWPalette(){
		
	}
	
	public void setStep(int _step){
		//step = _step;
	}
	
	public void addColorStep(float value, int R, int G, int B, boolean isSolid){
		values[color_num] = value;
		red_steps[color_num] = R;
		green_steps[color_num] = G;
		blue_steps[color_num] = B;
		if (isSolid){
			red_interp_end[color_num] = red_steps[color_num];
			green_interp_end[color_num] = green_steps[color_num];
			blue_interp_end[color_num] = blue_steps[color_num];
		}
		else{
			red_interp_end[color_num] = -1;
			green_interp_end[color_num] = -1;
			blue_interp_end[color_num] = -1;
		}
		color_num++;
	}
	
	public void addColorStep(float value, int R, int G, int B, int r_end, int g_end, int b_end){
		values[color_num] = value;
		red_steps[color_num] = R;
		green_steps[color_num] = G;
		blue_steps[color_num] = B;
		red_interp_end[color_num] = r_end;
		green_interp_end[color_num] = g_end;
		blue_interp_end[color_num] = b_end;
		color_num++;
	}
		
	public void addRFColor(int R, int G, int B){
		//RF = (R << 16)+(G << 8)+B;
	}
	
	public void sort(){
		int temp;
		for (int i=0; i < color_num - 1; i++){
			for (int n = i + 1; n < color_num; n++){
				if (values[i] > values[n]){
					float tempf = values[n];
					values[n] = values[i];
					values[i] = tempf;
					
					temp = red_steps[n];
					red_steps[n] = red_steps[i];
					red_steps[i] = temp;
					temp = green_steps[n];
					green_steps[n] = green_steps[i];
					green_steps[i] = temp;
					temp = blue_steps[n];
					blue_steps[n] = blue_steps[i];
					blue_steps[i] = temp;
					
					temp = red_interp_end[n];
					red_interp_end[n] = red_interp_end[i];
					red_interp_end[i] = temp;
					temp = green_interp_end[n];
					green_interp_end[n] = green_interp_end[i];
					green_interp_end[i] = temp;
					temp = blue_interp_end[n];
					blue_interp_end[n] = blue_interp_end[i];
					blue_interp_end[i] = temp;
				}
			}
		}
	}	
	
	public void setInterpValues(int[] thresh){
		
	}
	
	
	public int interpolate(int level){
		if (level == 0 || level == 1)
			return 0;
		float interp_value = (((float)level)*increment + min_value);
		int index = 0;
		for (int i=0; i<color_num; i++){
			if (values[i] > interp_value) break;
			index++;
		}
		if (interp_value == values[index])
			return (red_steps[index] << 16) + (green_steps[index] << 8) + blue_steps[index];
		float start_value = values[index];
		float end_value;
		if (index == color_num)
			if (red_interp_end[color_num] < 0 || green_interp_end[color_num] < 0 || blue_interp_end[color_num] < 0)
				return (red_steps[color_num] << 16) + (green_steps[color_num] << 8) + blue_steps[color_num];
			else
				end_value = max_value;
		else
			end_value = values[index+1];
		
		//float level_delta = end_value - start_value;
		float scaling_factor = (interp_value - start_value)/(end_value - start_value);
		int R, G, B;
		if (red_interp_end[index] < 0 || green_interp_end[index] < 0 || blue_interp_end[index] < 0){
			R = (int)((red_steps[index + 1] - red_steps[index])*scaling_factor + red_steps[index]) << 16;
			G = (int)((green_steps[index + 1] - green_steps[index])*scaling_factor + green_steps[index]) << 8;
			B = (int)((blue_steps[index + 1] - blue_steps[index])*scaling_factor + blue_steps[index]);
		}
		else{
			R = (int)((red_interp_end[index] - red_steps[index])*scaling_factor + red_steps[index]) << 16;
			G = (int)((green_interp_end[index] - green_steps[index])*scaling_factor + green_steps[index]) << 8;
			B = (int)((blue_interp_end[index] - blue_steps[index])*scaling_factor + blue_steps[index]);
		}
		
		return R+G+B;
	}

	public float getValue(int level) {
		return (((float)level)*increment + min_value);
	}
	
	public float getIncrement() {
		return increment;
	}

	public float getMinValue() {
		return min_value;
	}

	public void setUnits(Units units) {
		// TODO Auto-generated method stub
		
	}

	public Units getUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasCompatibleUnits(Units unit) {
		// TODO Auto-generated method stub
		return false;
	}

	public Type getType() {
		return Type.SW;
	}
}
