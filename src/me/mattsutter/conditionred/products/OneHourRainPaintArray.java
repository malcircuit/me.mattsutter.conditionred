package me.mattsutter.conditionred.products;

import me.mattsutter.conditionred.products.Palette.Units;
import static me.mattsutter.conditionred.products.LegacyPalette.*;

public class OneHourRainPaintArray implements PaintArray {
	private static final int COLOR_NUM = 16;
	private static final float CM_PER_INCH = 2.54f;
	
	private final int[] colors;
	private final Units units;
	private final MODIFIERS[][] modifiers;
	private final int[] thresh_values;
	
	public OneHourRainPaintArray(LegacyPalette precip_pal, int[] thresh) {
		precip_pal.setInterpValues(thresh);
		thresh_values = thresh;
		modifiers = precip_pal.getModifiers();
		units = precip_pal.getUnits();
		
		colors = new int[COLOR_NUM];
		for (int i = 0; i < COLOR_NUM; i++)
			colors[i] = precip_pal.interpolate(i);
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
			return interp_value * CM_PER_INCH * 10.0f;
		if (units == Palette.Units.FT)
			return interp_value / 12.0f;
		return interp_value;
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
