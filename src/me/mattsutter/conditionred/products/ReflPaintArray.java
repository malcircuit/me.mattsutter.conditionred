package me.mattsutter.conditionred.products;

public final class ReflPaintArray implements PaintArray{
	private static final int COLOR_NUM = 256;
	private final int[] colors;
	private final float increment, min_value;
	
	public ReflPaintArray(ReflPalette refl_pal, int[] thresh) {
		refl_pal.setInterpValues(thresh);
		
		increment = refl_pal.getIncrement();
		min_value = refl_pal.getMinValue();
		

		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = refl_pal.interpolate(i);
	}
	
	public float getValue(int level) {
		return (((float)level - 2.0f)*increment + min_value);
	}

	public String getUnits() {
		return "dBZ";
	}
	
	public int[] getColors() {
		return colors;
	}
	
	public int getColorNum() {
		return COLOR_NUM;
	}
}
