package me.mattsutter.conditionred;

import android.graphics.Point;
import android.graphics.PointF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import me.mattsutter.conditionred.util.RenderCommand;

import static me.mattsutter.conditionred.CustomMapView.getRadarWidth;

public class MapChangeCommand implements RenderCommand {

	public final PointF radar_center;
	public final float radar_width;
	public final boolean site_change;
	public final long time_sent;
	
	public MapChangeCommand(MapView map_view, int prod_code, GeoPoint center, boolean site_has_changed){
		Point temp = map_view.getProjection().toPixels(center, null);
		radar_center = new PointF((float) temp.x, (float) temp.y);
		radar_width = getRadarWidth(map_view, prod_code, center);
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
