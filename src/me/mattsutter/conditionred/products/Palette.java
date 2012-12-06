package me.mattsutter.conditionred.products;

public interface Palette {
	static enum Type{REFL, VEL, VIL, SW, ET, ONE_HR_RAIN, SRV, STRM_TOTAL};
	static enum Units{KTS, MPH, MPS, KMPH, DBZ, IN, CM, FT, M, KGM2, LBFT2, KFT, KM, MI, MM};
	
	static final int ND = 0;
	static final int TH = 1;
	static final int MAX_STEPS = 50;
	
	void setStep(int _step);
	void setUnits(Units units);
	void addColorStep(float value, int R, int G, int B, boolean isSolid);
	void addColorStep(float value, int R, int G, int B, int r_end, int g_end, int b_end);
	void addRFColor(int R, int G, int B);
	void sort();
	void setInterpValues(int[] thresh);
	int interpolate(int level);
	float getIncrement();
	float getMinValue();
	Units getUnits();
	Type getType();
	boolean hasCompatibleUnits(Units unit);
}
