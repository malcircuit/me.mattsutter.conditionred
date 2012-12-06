package me.mattsutter.conditionred.products;

/**
 * Applies to products 32 (Digital Hybrid Scan Reflectivity), 94 (Digital Base Reflectivity), 
 * 153 (Super Res Base Reflectivity), 194 (Digital Base Reflectivity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class EchoTopPalette implements Palette{
	
	//private int step;
	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	private int color_num = 0;
	private Units units;
	
	// These are the values in thresh, so we can ignore implementing setInterpValues()
	protected static final int DATA_MASK = 0x7f;
	protected static final int DATA_OFFSET = 2;
	protected static final int DATA_SCALE = 1;
	protected static final int TOPPED_MASK = 0x80;
//	private static final int MAX_KFT = 70; // ET maxes out at 70,000 ft.
	
	public static final float M_PER_FT = 0.3048f;
	public static final int FT_PER_KFT = 1000;
	public static final float M_PER_KFT = M_PER_FT * FT_PER_KFT;
	
	public EchoTopPalette(){
		
	}
	
	public void setStep(int _step){
		
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
	
	public int interpolate(int level){
		if (level == 0 || level == 1)
			return 0;
		
		float interp_value = getValue(level);
		if (interp_value < values[0])
			return 0;
		if (interp_value >= values[color_num - 1])
			return (red_steps[color_num - 1] << 16) + (green_steps[color_num - 1] << 8) + blue_steps[color_num - 1];

		int index = 0;
		for (int i=0; i<color_num; i++){
			if (interp_value < values[i])
				break;
			else
				index = i;
		}
		
		if (interp_value == values[index])
			return (red_steps[index] << 16) + (green_steps[index] << 8) + blue_steps[index];
		
		float start_value = values[index];
		float end_value;
		if (index == color_num)
			return (red_steps[color_num] << 16) + (green_steps[color_num] << 8) + blue_steps[color_num];
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
		if (units == Palette.Units.M)
			return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET) * M_PER_KFT;
		if (units == Palette.Units.KM)
			return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET) * M_PER_FT;
		return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET);
	}
	
	protected boolean isTopped(int level){
		if ((level & TOPPED_MASK) != 0) return true;
		return false;
	}
	

	public float getIncrement() {
		return 0;
	}

	public float getMinValue() {
		return 0;
	}

	public void setUnits(Units units) {
		this.units = units;
	}

	public Units getUnits() {
		return units;
	}

	public boolean hasCompatibleUnits(Units unit) {
		return unit == Units.KFT || unit == Units.KM || unit == Units.M;
	}

	public Type getType() {
		return Type.ET;
	}

	public void setInterpValues(int[] thresh) {
		// The values in thresh for this product are constants, so don't need to bother reading them in.
	}
	
}
