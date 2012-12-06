package me.mattsutter.conditionred.products;

public final class VILPaintArray implements PaintArray{
	private static final int COLOR_NUM = 256;
	private static final float LBFT2_PER_KGM2 = 0.2048f;
	
	private final int[] colors;
	private final float linear_scale;
	private final float linear_offset;
	private final int log_start;
	private final float log_scale;
	private final float log_offset;
	private final Palette.Units units;
	
	public VILPaintArray(VILPalette vil_pal, int[] thresh) {
		vil_pal.setInterpValues(thresh);

		linear_scale = vil_pal.getLinearScale();
		linear_offset = vil_pal.getLinearOffset();
		log_start = vil_pal.getLogStart();
		log_scale = vil_pal.getLogScale();
		log_offset = vil_pal.getLogOffset();
		units = vil_pal.getUnits();
		

		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = vil_pal.interpolate(i);
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

	public String getUnits() {
		if (units == Palette.Units.LBFT2)
			return "lb/ft2";
		return "kg/m2";
	}

	public int[] getColors() {
		return colors;
	}
	
	public int getColorNum() {
		return COLOR_NUM;
	}
}
