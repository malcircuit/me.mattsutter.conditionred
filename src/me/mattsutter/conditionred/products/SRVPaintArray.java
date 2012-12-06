package me.mattsutter.conditionred.products;

public final class SRVPaintArray implements PaintArray {
	private static final int COLOR_NUM = 256;
	private static final float KTS_PER_MPS = 1.944f;
	private static final float MPH_PER_MPS = 2.237f;
	private static final float KMPH_PER_MPS = 3.6f;
	
	private final int[] colors;
	private final float increment, min_value;
	private final Palette.Units units;
	
	public SRVPaintArray(SRVPalette srv_pal, int[] thresh, int p8, int p9) {
		srv_pal.setInterpValues(thresh);

		increment = srv_pal.getIncrement();
		min_value = srv_pal.getMinValue();
		units = srv_pal.getUnits();
		
		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = srv_pal.interpolate(i);
	}
	
	public float getValue(int level) {
//		Log.d("PAINTS", "min value: " + Float.toString(min_value) + " inc: " + Float.toString(increment));
		if (units == Palette.Units.MPS)
			return (((float)level - 2.0f)*increment + min_value);
		if (units == Palette.Units.MPH)
			return (((float)level - 2.0f)*increment + min_value) * MPH_PER_MPS;
		if (units == Palette.Units.KMPH)
			return (((float)level - 2.0f)*increment + min_value) * KMPH_PER_MPS;
		return (((float)level - 2.0f)*increment + min_value) * KTS_PER_MPS;
	}

	public String getUnits() {
		if (units == Palette.Units.MPS)
			return "m/s";
		if (units == Palette.Units.MPH)
			return "mph";
		if (units == Palette.Units.KMPH)
			return "km/h";
		return "kts";
	}
	
	public int[] getColors() {
		return colors;
	}
	
	public int getColorNum() {
		return COLOR_NUM;
	}
}
