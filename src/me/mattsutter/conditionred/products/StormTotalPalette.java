package me.mattsutter.conditionred.products;

/**
 * Applies to products 32 (Digital Hybrid Scan Reflectivity), 94 (Digital Base Reflectivity), 
 * 153 (Super Res Base Reflectivity), 194 (Digital Base Reflectivity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class StormTotalPalette implements Palette{

	//private int step;
	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	private int color_num = 0;
	private int min_value;
	private float increment;
	private Units units;

	private static final float CM_PER_INCH = 2.54f;
	
	public StormTotalPalette(){
		
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
		min_value = thresh[0];
		final short incr = (short) thresh[1];
		increment = ((float)incr)/100.0f;		
	}
	
	
	public int interpolate(int level){
		if (level == 0)
			return 0;
		float interp_value = getValue(level);
		if ((values[0] - interp_value) >= 0.001f)
			return 0;
		if (interp_value >= values[color_num - 1])
			return (red_steps[color_num - 1] << 16) + (green_steps[color_num - 1] << 8) + blue_steps[color_num -1 ];

		int index = 0;// = -1;
		for (int i=0; i < color_num; i++){
			if ((values[i] - interp_value) > 0.001f)
				break;
			else
				index = i;
		}
		if ((values[index] - interp_value) < 0.01f)
			return (red_steps[index] << 16) + (green_steps[index] << 8) + blue_steps[index];
			
		float start_value = values[index];
		float end_value;
		if (index == color_num)
			return (red_steps[color_num] << 16) + (green_steps[color_num] << 8) + blue_steps[color_num];
		else
			end_value = values[index + 1];
		
		float scaling_factor = (interp_value - start_value)/(end_value - start_value);
		int R, G, B;
		if (red_interp_end[index] < 0 || green_interp_end[index] < 0 || blue_interp_end[index] < 0){
			R = (int)((float)(red_steps[index + 1] - red_steps[index])*scaling_factor + (float)red_steps[index] + 0.5f) << 16;
			G = (int)((float)(green_steps[index + 1] - green_steps[index])*scaling_factor + (float)green_steps[index] + 0.5f) << 8;
			B = (int)((float)(blue_steps[index + 1] - blue_steps[index])*scaling_factor + (float)blue_steps[index] + 0.5f);
		}
		else{
			R = (int)((float)(red_interp_end[index] - red_steps[index])*scaling_factor + (float)red_steps[index] + 0.5f) << 16;
			G = (int)((float)(green_interp_end[index] - green_steps[index])*scaling_factor + (float)green_steps[index] + 0.5f) << 8;
			B = (int)((float)(blue_interp_end[index] - blue_steps[index])*scaling_factor + (float)blue_steps[index] + 0.5f);
		}
		
		return R+G+B;
	}

	public float getValue(int level) {
		if (units == Palette.Units.CM)
			return (((float)level - 1.0f)*increment + min_value) * CM_PER_INCH;
		if (units == Palette.Units.MM)
			return (((float)level - 1.0f)*increment + min_value) * CM_PER_INCH * 10.0f;
		if (units == Palette.Units.FT)
			return (((float)level - 1.0f)*increment + min_value) / 12.0f;
		// Default is inches.
		return (((float)level - 1.0f)*increment + min_value);
	}
	

	public float getIncrement() {
		return increment;
	}

	public float getMinValue() {
		return min_value;
	}

	public void setUnits(Units units) {
		this.units = units;		
	}

	public Units getUnits() {
		return units;
	}

	public boolean hasCompatibleUnits(Units unit) {
		return unit == Units.CM || unit == Units.MM || unit == Units.FT;
	}

	public Type getType() {
		return Type.STRM_TOTAL;
	}
}
