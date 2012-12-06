package me.mattsutter.conditionred.products;

/**
 * Applies to Products 93 (ITWS Digital Base Velocity), 99 (Digital Base Velocity),
 * 154 (Super Res Base Velocity), 155 (Super Res Spectrum Width), 
 * and 199 (Digital Base Velocity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class VelPalette implements Palette {
	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	private int RF = -1;
	private int color_num = 0;
	private float min_value;
	private float increment;
	private Units units;

	private static final float KTS_PER_MPS = 1.944f;
	private static final float MPH_PER_MPS = 2.237f;
	private static final float KMPH_PER_MPS = 3.6f;
	
	public VelPalette(){
		
	}
	
	public void addColorStep(float value, int R, int G, int B, boolean isSolid) {
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

	public void addColorStep(float value, int R, int G, int B, int rEnd, int gEnd, int bEnd) {
		values[color_num] = value;
		red_steps[color_num] = R;
		green_steps[color_num] = G;
		blue_steps[color_num] = B;
		red_interp_end[color_num] = rEnd;
		green_interp_end[color_num] = gEnd;
		blue_interp_end[color_num] = bEnd;
		color_num++;

	}
	
	public void setInterpValues(int[] thresh){
		final short min_val = (short)thresh[0];
		final short incr = (short)thresh[1];
		
		min_value = ((float)min_val)/10.0f;
		increment = ((float)incr)/10.0f;
	}

	public int interpolate(int level) {
		if (level == 0)
			return 0;
		if (level == 1)
			if (RF >= 0)
				return RF;
			else
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

	public void setStep(int _step) {
		
	}

	public void sort() {
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

	public void addRFColor(int R, int G, int B) {
		RF = (R << 16)+(G << 8)+B;
	}
	public float getValue(int level) {
		if (units == Palette.Units.MPS)
			return (((float)level - 2.0f)*increment + min_value);
		if (units == Palette.Units.MPH)
			return (((float)level - 2.0f)*increment + min_value) * MPH_PER_MPS;
		if (units == Palette.Units.KMPH)
			return (((float)level - 2.0f)*increment + min_value) * KMPH_PER_MPS;
		return (((float)level - 2.0f)*increment + min_value) * KTS_PER_MPS;
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
		return unit == Units.MPH || unit == Units.KMPH || unit == Units.KTS || unit == Units.MPS;
	}

	public Type getType() {
		return Type.VEL;
	}
}
