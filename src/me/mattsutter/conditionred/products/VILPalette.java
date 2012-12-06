package me.mattsutter.conditionred.products;

/**
 * Applies to products 32 (Digital Hybrid Scan Reflectivity), 94 (Digital Base Reflectivity), 
 * 153 (Super Res Base Reflectivity), 194 (Digital Base Reflectivity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class VILPalette implements Palette{

	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	private int color_num = 0;
	
	private static final float LBFT2_PER_KGM2 = 0.2048f;
	
	private float linear_scale;
	private float linear_offset;
	private int log_start;
	private float log_scale;
	private float log_offset;
	private Units units;
	
	/*private static final float LINEAR_SCALE = 90.6875f;
	private static final float LINEAR_OFFSET = 2.0f;
	private static final float LOG_OFFSET = 83.875f;
	private static final float LOG_SCALE = 35.4375f;
	private static final int LOG_START = 20;
	private static final float MAX_VIL = 121.6f; // kg/m2
	private static final float VIL_LIMIT = 80.0f;*/
	//private int num_levels;
	//private static final int OPAQUE = 0xFF000000;
	
	public VILPalette(){
		
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
		linear_scale = shortToFloat((short)thresh[0]);
		linear_offset = shortToFloat((short)thresh[1]);
		log_start = thresh[2];
		log_scale = shortToFloat((short)thresh[3]);
		log_offset = shortToFloat((short)thresh[4]);
	}
	
	protected float getLinearScale(){
		return linear_scale;
	}
	
	protected float getLinearOffset(){
		return linear_offset;
	}
	
	protected int getLogStart(){
		return log_start;
	}
	
	protected float getLogScale(){
		return log_scale;
	}
	
	protected float getLogOffset(){
		return log_offset;
	}
	
	public int interpolate(int level){
		if (level == 0 || level == 1)
			return 0;
		
		float interp_value = getValue(level);
		if (interp_value < 0)
			return 0;
		if (interp_value < values[0])
			return 0;
		if (interp_value >= values[color_num - 1])
			return (red_steps[color_num - 1] << 16) + (green_steps[color_num - 1] << 8) + blue_steps[color_num - 1];
		
		int index = 0;
		for (int i=0; i < color_num; i++){
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
		float value;
		if (level >= log_start)
			value = (float) (Math.exp((double)(((float)level-log_offset)/log_scale)));
		else
			value = ((float)level-linear_offset)/linear_scale;
		
		if (units == Palette.Units.LBFT2)
			return value * LBFT2_PER_KGM2;
		return value;
	}
	
	/**
	 * Converts a 16-bit unsigned integer value into a 32-bit floating-point value.
	 * @param number - 16-bit unsigned integer.  Use Integer because all integer types in Java are signed --  
	 * Short may cause problems.
	 * @return Floating-point value.
	 */
	private float shortToFloat(int number){
		int sign = (number >> 15);
		int exponent = (number >> 10) - (sign << 6);
		int fraction = number - ((number >> 10) << 10);
		if (exponent == 0){
			return (float)(Math.pow((-1), sign) * 2 * (0 + (fraction/Math.pow(2, 10))));
		}
		return (float)(Math.pow((-1), sign) * Math.pow(2, (exponent - 16)) * (1 + (fraction/Math.pow(2, 10))));
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
		return unit == Units.LBFT2 || unit == Units.KGM2;
	}

	public Type getType() {
		return Type.VIL;
	}
}
