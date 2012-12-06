package me.mattsutter.conditionred.products;

/**
 * Applies to Products 93 (ITWS Digital Base Velocity), 99 (Digital Base Velocity),
 * 154 (Super Res Base Velocity), 155 (Super Res Spectrum Width), 
 * and 199 (Digital Base Velocity DoD Version).
 * 
 * @author Matt Sutter
 *
 */
public class LegacyPalette implements Palette {
	private int[] red_steps = new int[MAX_STEPS];
	private int[] green_steps = new int[MAX_STEPS];
	private int[] blue_steps = new int[MAX_STEPS];
	private float[] values = new float[MAX_STEPS];
	private int[] red_interp_end = new int[MAX_STEPS];
	private int[] green_interp_end = new int[MAX_STEPS];
	private int[] blue_interp_end = new int[MAX_STEPS];
	private int RF = -1;
	private int color_num = 0;
	private int[] thresh_values = new int[16];
	private Units units;
	private Type type;
	
	protected static final int TYPE_OFFSET = 8;
	protected static final int MAX_MODIFIERS = 4;
	protected static final int LEVEL_NUM = 16;
	protected static final int TYPE = 0;
	protected static final int SCALE = 1;
	protected static final int MORE_LESS = 2;
	protected static final int PLUS_MINUS = 3;

	protected static final float CM_PER_INCH = 2.54f;
	protected static final float KTS_PER_MPS = 1.944f;
	protected static final float MPH_PER_MPS = 2.237f;
	protected static final float KMPH_PER_MPS = 3.6f;
	
	protected static final int LS_BYTE_MASK = 0xFF;
	protected static final short[] MASKS = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
	
	protected static enum MODIFIERS{	TYPE,			// Specific radar return type 
										SCALE_BY_100, 	// Scale the least significant byte by 100
										SCALE_BY_20, 	// Scale the least significant byte by 20
										SCALE_BY_10, 	// Scale the least significant byte by 10
										GREATER_THAN, 	// Value is greater than least significant byte
										LESS_THAN, 		// Value is less than least significant byte
										PLUS, 			// Least significant byte is positive
										MINUS,			// Least significant byte is negative
										////////////////// TYPES /////////////////////////////////////
										BLANK,			// Blank
										TH,				// Below Threshold
										ND,				// Not Detectable
										RF,				// Range folded
										BI,				// Biological
										GC,				// Anomalous Propagation/Ground Clutter
										IC,				// Ice Crystals
										GR,				// Graupel
										WS,				// Wet Snow
										DS,				// Dry Snow
										RA,				// Light and Moderate Rain
										HR,				// Heavy Rain
										BD,				// Big Drops
										HA,				// Hail and Rain Mixed
										UK				// Unknown
	};

	private MODIFIERS[][] modifiers = new MODIFIERS[LEVEL_NUM][MAX_MODIFIERS];
	
	public LegacyPalette(Type type){
		this.type = type;
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
		MODIFIERS[] mods = MODIFIERS.values();
		for (int n=0; n < LEVEL_NUM; n++){
			for (int i=0; i < MAX_MODIFIERS; i++){
				modifiers[n][i] = MODIFIERS.BLANK;
			}
		}
		
		for (int n=0; n < LEVEL_NUM; n++){
			thresh_values[n] = (thresh[n] & LS_BYTE_MASK);
			
			for (int i=0; i < 8; i++){
				boolean matches  = (((thresh[n] >>> 8) & MASKS[i]) == MASKS[i]);
				if (matches){
					switch(i){
					case 0:
						modifiers[n][TYPE] = mods[thresh_values[n] + TYPE_OFFSET];
						break;
					case 1:
					case 2:
					case 3:
						modifiers[n][SCALE] = mods[i];
						i = 3;
						break;
					case 4:
					case 5:
						modifiers[n][MORE_LESS] = mods[i];
						i = 5;
						break;
					case 6:
					case 7:
						modifiers[n][PLUS_MINUS] = mods[i];
						i = 7;
						break;
					}
				}
			}
		}
	}

	public int interpolate(int level) {
	
		switch(modifiers[level][TYPE]){
		case TH:
			return 0;
		case ND:
			return 0;
		case RF:
			if (RF >= 0)
				return RF;
		//case BLANK:
		case BI:
		case IC:
		case GC:
		case GR:
		case WS:
		case DS:
		case RA:
		case HR:
		case BD:
		case HA:
		case UK:
			return 0;
		default:
			break;
		}

		float interp_value = (float)thresh_values[level];
		switch(modifiers[level][SCALE]){
		case SCALE_BY_100:
			interp_value = interp_value/100.0f;
			break;
		case SCALE_BY_20:
			interp_value = interp_value/20.0f;
			break;
		case SCALE_BY_10:
			interp_value = interp_value/10.0f;
			break;
		}

		/*switch(modifiers[level][MORE_LESS]){
		case GREATER_THAN:
		case LESS_THAN:
		}*/

		switch(modifiers[level][PLUS_MINUS]){
		case MINUS:
			interp_value = interp_value * (-1.0f);
			break;
		}
		
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
		if (interp_value == values[index]) //|| modifiers[index][MORE_LESS] == MODIFIERS.LESS_THAN)
			return (red_steps[index] << 16) + (green_steps[index] << 8) + blue_steps[index];
		
		float start_value = values[index];
		float end_value;
		if (index == color_num)
			return (red_steps[color_num] << 16) + (green_steps[color_num] << 8) + blue_steps[color_num];
		else
			//if (modifiers[index][MORE_LESS] == MODIFIERS.GREATER_THAN)
			//	return (red_steps[index + 1] << 16) + (green_steps[index + 1] << 8) + blue_steps[index + 1];
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
		switch(modifiers[level][TYPE]){
		case TH:
		case ND:
		case RF:
		case BLANK:
		case BI:
		case IC:
		case GC:
		case GR:
		case WS:
		case DS:
		case RA:
		case HR:
		case BD:
		case HA:
		case UK:
			return 0;
		default:
			break;
		}

		float interp_value = (float)thresh_values[level];
		switch(modifiers[level][SCALE]){
		case SCALE_BY_100:
			interp_value = interp_value/100.0f;
			break;
		case SCALE_BY_20:
			interp_value = interp_value/20.0f;
			break;
		case SCALE_BY_10:
			interp_value = interp_value/10.0f;
			break;
		}

		switch(modifiers[level][PLUS_MINUS]){
		case MINUS:
			interp_value *= -1.0f;
			break;
		}
		
		if (units == Palette.Units.CM)
			return interp_value * CM_PER_INCH;
		if (units == Palette.Units.MM)
			return interp_value * CM_PER_INCH * 10;
		if (units == Palette.Units.FT)
			return interp_value / 12;
		if (units == Palette.Units.MPH)
			return interp_value * MPH_PER_MPS;
		if (units == Palette.Units.KMPH)
			return interp_value * KMPH_PER_MPS;
		if (units == Palette.Units.KTS)
			return interp_value * KTS_PER_MPS;
		return interp_value;
	}

	protected int[] getThresh(){
		return thresh_values;
	}
	
	protected MODIFIERS[][] getModifiers(){
		return modifiers;
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
		switch (units){
		case CM:
		case MM:
		case FT:
			return unit == Units.CM || unit == Units.MM || unit == Units.FT;
		default:
			return unit == Units.MPH || unit == Units.KMPH || unit == Units.KTS || unit == Units.MPS;
		}
	}

	public Type getType() {
		return type;
	}
}
