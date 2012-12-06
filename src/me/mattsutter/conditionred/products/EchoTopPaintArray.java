package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.products.EchoTopPalette.*;

public final class EchoTopPaintArray implements PaintArray{
	private static final int COLOR_NUM = 256;
	private final int[] colors;
	private final Palette.Units units;
	
	public EchoTopPaintArray(EchoTopPalette et_pal) {
		units = et_pal.getUnits();
		
		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = et_pal.interpolate(i);
	}
	
	public float getValue(int level) {
		if (units == Palette.Units.M)
			return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET) * M_PER_KFT;
		if (units == Palette.Units.KM)
			return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET) * M_PER_FT;
		return (float)((level & DATA_MASK)/DATA_SCALE - DATA_OFFSET);
	}
	
	protected boolean isTopped(int level){
		if ((level & TOPPED_MASK) != 0) 
			return true;
		return false;
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
