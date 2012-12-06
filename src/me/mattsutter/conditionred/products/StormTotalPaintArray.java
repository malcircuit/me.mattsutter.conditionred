package me.mattsutter.conditionred.products;

public class StormTotalPaintArray implements PaintArray {
	private static final int COLOR_NUM = 256;
	private static final float CM_PER_INCH = 2.54f;
	
	private final int[] colors;
	private final float increment, min_value;
	private final Palette.Units units;
	
	public StormTotalPaintArray(StormTotalPalette precip_pal, int[] thresh) {
		precip_pal.setInterpValues(thresh);
		
		increment = precip_pal.getIncrement();
		min_value = precip_pal.getMinValue();
		units = precip_pal.getUnits();

		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = precip_pal.interpolate(i);
	}

	public float getValue(int level) {
		if (units == Palette.Units.CM)
			return (((float)level - 1.0f)*increment + min_value) * CM_PER_INCH;
		if (units == Palette.Units.MM)
			return (((float)level - 1.0f)*increment + min_value) * CM_PER_INCH * 10.0f;
		if (units == Palette.Units.FT)
			return (((float)level - 1.0f)*increment + min_value) / 12.0f;
		return (((float)level - 1.0f)*increment + min_value);
	}

	public String getUnits() {
		if (units == Palette.Units.CM)
			return "cm";
		if (units == Palette.Units.MM)
			return "mm";
		if (units == Palette.Units.FT)
			return "ft";
		return "in";
	}
	
	public int[] getColors() {
		return colors;
	}
	
	public int getColorNum() {
		return COLOR_NUM;
	}
}
