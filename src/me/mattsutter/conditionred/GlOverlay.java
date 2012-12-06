package me.mattsutter.conditionred;

import static me.mattsutter.conditionred.CustomMapView.getRadarWidth;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.mattsutter.conditionred.util.ProductManager;
import me.mattsutter.conditionred.util.RenderCommand;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GlOverlay extends Overlay {
	
	private static final int MAX_FRAMES = 15;
	
	private final RadarView gl_view;
	private Point center = new Point();
	private GeoPoint radar_center;
	private int prod_code = 0;
	private float radar_width = 0;
	private String site_id = "";
	private String prod_url = "";
	private final ConcurrentLinkedQueue<RenderCommand> queue = new ConcurrentLinkedQueue<RenderCommand>();
	private final CustomMapView map_view;
	private final ProductManager prod_man;
	
	public GlOverlay(Handler handler, Context context, CustomMapView map_view, GeoPoint radar_center){
		gl_view = new RadarView(context, queue, MAX_FRAMES);
		
		map_view.addView(gl_view, new MapView.LayoutParams(
				MapView.LayoutParams.FILL_PARENT, 
				MapView.LayoutParams.FILL_PARENT, 
				0,0, 
				MapView.LayoutParams.TOP_LEFT)
		);
		
		map_view.getOverlays().add(this);
		this.radar_center = radar_center;
		this.map_view = map_view;
		
		prod_man = new ProductManager(context, MAX_FRAMES, true, handler, queue);
	}

	public void draw(Canvas canvas, MapView map_view, boolean shadow){
		if (!shadow){
			final boolean center_has_changed = centerHasChanged(map_view);
			final boolean width_has_changed = radarWidthHasChanged(map_view);
			if (center_has_changed || width_has_changed){
				mapHasChanged(radar_center);
			}
		}
	}
	
	public void onPause(){
		gl_view.onPause();
	}
	
	public void onResume(int prod_code, String site_id, String prod_url){
		prod_man.onResume();
		final boolean site_has_changed = checkForSiteChange(site_id);
		final boolean prod_has_changed = checkForProductChange(prod_code, prod_url);
		if (site_has_changed || prod_has_changed){
			Log.d("GLOverlay", "Product or site has changed.");
			prod_man.productChange(prod_url, site_id);
			prod_man.startAnimation();
		}
		
		gl_view.onResume();
	}
	
	public void onDestroy(){
		gl_view.onDestroy();
		prod_man.onDestroy();
	}
	
	private boolean checkForProductChange(int prod_code, String prod_url){
		final boolean prod_has_changed = this.prod_code != prod_code;
		final boolean url_has_changed = !prod_url.equals(this.prod_url);
		if (prod_has_changed){
			queue.add(new ProductChangeCommand(prod_code));
			this.prod_code = prod_code;
		}
		if (url_has_changed)
			this.prod_url = prod_url;
		
		return url_has_changed || prod_has_changed;
	}
	
	private boolean checkForSiteChange(String site){
		final boolean has_changed = !site_id.equals(site);
		if (has_changed){
			queue.add(new MapChangeCommand(map_view, prod_code, radar_center, true));
			site_id = site;
		}
		
		return has_changed;
	}
	
	protected void changeImageAlpha(short alpha){
		queue.add(new AlphaChangeCommand(alpha));
	}
	
	protected void mapHasChanged(GeoPoint new_center){
		radar_center = new_center;
		queue.add(new MapChangeCommand(map_view, prod_code, radar_center, false));
	}
	
	/**
	 * Finds the point on the screen that it the center of the radar image.
	 * @param map_view - MapView to get the projection from.
	 */
	private boolean centerHasChanged(MapView map_view){
		final Point new_center = map_view.getProjection().toPixels(radar_center, null);
		if (new_center.x != center.x || new_center.y != center.y){
			center = new Point(new_center);
			return true;
		}
		return false;
	}
	
	/**
	 * Computes the width (in pixels) of the radar image based on the current projection
	 * of the MapView.
	 * @param map_view - MapView to get the projection of.
	 */
	private boolean radarWidthHasChanged(MapView map_view){
		final float new_radar_width = getRadarWidth(map_view, prod_code, radar_center);
		
		if (radar_width != new_radar_width){
			radar_width = new_radar_width;
			return true;
		}
		return false;
    }
}