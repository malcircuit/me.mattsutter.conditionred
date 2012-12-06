package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.products.LegacyPalette.*;
//import android.util.Log;

public final class LegacySRVPaintArray implements PaintArray {
	private static final int COLOR_NUM = 16;
	private final int[] colors;
	private final Palette.Units units;
	private final MODIFIERS[][] modifiers;
	private final int[] thresh_values;
	
	public LegacySRVPaintArray(LegacyPalette srv_pal, int[] thresh) {
		srv_pal.setInterpValues(thresh);
		thresh_values = thresh;
		modifiers = srv_pal.getModifiers();

		units = srv_pal.getUnits();
		
		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = srv_pal.interpolate(i);
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
		
		if (units == Palette.Units.MPH)
			return interp_value * MPH_PER_MPS;
		if (units == Palette.Units.KMPH)
			return interp_value * KMPH_PER_MPS;
		if (units == Palette.Units.KTS)
			return interp_value * KTS_PER_MPS;
		return interp_value;
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
