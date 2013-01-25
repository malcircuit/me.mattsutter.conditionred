package me.mattsutter.conditionred;

import android.graphics.PointF;
import me.mattsutter.conditionred.util.LatLng;
import me.mattsutter.conditionred.util.RenderCommand;

public class MapChangeCommand implements RenderCommand {

	public final PointF radar_center;
	public final boolean site_change;
	public final long time_sent;
	
	public MapChangeCommand(int prod_code, LatLng center, boolean site_has_changed){
		radar_center = center.toPixels();
		site_change = site_has_changed;
		time_sent = System.currentTimeMillis();
	}

	public Type getType() {
		return Type.MAP_CHANGE;
	}

	public long getTimeSent() {
		return time_sent;
	}
}
