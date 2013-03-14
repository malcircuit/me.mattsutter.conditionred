package me.mattsutter.conditionred.graphics;

import me.mattsutter.conditionred.util.LatLng;
import me.mattsutter.conditionred.graphics.RenderCommand;

public class PanCommand implements RenderCommand {

	public final boolean site_change;
	public final float dx, dy;
	public final LatLng new_center;
	
	public PanCommand(LatLng center){
		site_change = true;
		new_center = center;
		dx = Float.NaN;
		dy = Float.NaN;
	}
	
	public PanCommand(float dx, float dy){
		site_change = false;
		this.dx = dx;
		this.dy = dy;
		new_center = new LatLng(0,0);
	}
	
	public Type getType() {
		return Type.PAN;
	}

}
