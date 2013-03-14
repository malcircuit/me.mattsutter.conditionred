package me.mattsutter.conditionred.graphics;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import me.mattsutter.conditionred.util.LatLng;

public abstract class MapProjection {
	
	private static final float MAX_LONG = -60.0f;
	private static final float MIN_LONG = -220.0f;
	private static final float MAX_LAT = 67.0f;
	private static final float MIN_LAT = 10.0f;
	private static final float LAT_SPREAD = MAX_LAT - MIN_LAT;
	private static final float LNG_SPREAD = MAX_LONG - MIN_LONG;
	private static final float MAP_BOTTOM = (float)LatLng.getMercatorY(MIN_LAT);
	private static final float MAP_TOP = (float)LatLng.getMercatorY(MAX_LAT);
	private static final float MAP_RIGHT = (float)LatLng.getMercatorX(MAX_LONG);
	private static final float MAP_LEFT = (float)LatLng.getMercatorX(MIN_LONG);
	
	private static final float MAP_WIDTH = MAP_RIGHT - MAP_LEFT;
	private static final float MAP_HEIGHT = MAP_TOP - MAP_BOTTOM;
	private static final float MAP_ASPECT = MAP_WIDTH/MAP_HEIGHT;
	private static final LatLng MAP_CENTER = new LatLng(MIN_LAT + LAT_SPREAD / 2.0f, MIN_LONG + LNG_SPREAD / 2.0f);
	
	private final LatLng vp_center = new LatLng(MAP_CENTER);

	private int screen_width, screen_height;
	private float screen_aspect;
	private float vp_top, vp_bottom, vp_left, vp_right;
	private float vp_width, vp_height;
	private float current_scale = 1.0f;
	private boolean is_init = false;

	protected float merc_per_pixel;
	
	public MapProjection(){
		
	}
	
	protected void onSurfaceChanged(GL10 gl, int screen_width, int screen_height){
		gl.glViewport(0, 0, screen_width, screen_height);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		
		final float map_top, map_bottom, map_left, map_right;
		
		this.screen_width = screen_width;
		this.screen_height = screen_height;
		screen_aspect = (float)screen_width/(float)screen_height;

		final PointF center = MAP_CENTER.mercator;
		final float map_scale_factor = screen_aspect / MAP_ASPECT;
		
		// So the map always has the same aspect ratio and is centered on the screen.
		if (screen_aspect < MAP_ASPECT){		// i.e., the screen is taller than the map
			map_top = (MAP_TOP - center.y) / map_scale_factor + center.y;
			map_bottom = center.y - (center.y - MAP_BOTTOM) / map_scale_factor;
			map_left = MAP_LEFT;
			map_right = MAP_RIGHT;
		}
		else if (screen_aspect > MAP_ASPECT){	//i.e., the screen is wider than the map.
			map_top = MAP_TOP;
			map_bottom = MAP_BOTTOM;
			map_left =  center.x - (center.x - MAP_LEFT) * map_scale_factor;
			map_right = (MAP_RIGHT - center.x) * map_scale_factor + center.x;
		}
		else{
			map_top = MAP_TOP;
			map_bottom = MAP_BOTTOM;
			map_left = MAP_LEFT;
			map_right = MAP_RIGHT;
		}
		
		gl.glOrthof(map_left, map_right, map_bottom, map_top, -1, 1);
		
		if (is_init){
			merc_per_pixel = (map_right - map_left) / (screen_width * current_scale);
			vp_width = screen_width * merc_per_pixel;
			vp_height = screen_height * merc_per_pixel;
			
			final float dy = vp_width / 2.0f;
			final float dx = vp_height / 2.0f;

			vp_top = vp_center.mercator.y + dy;
			vp_bottom = vp_center.mercator.y - dy;
			vp_right = vp_center.mercator.x + dx;
			vp_left = vp_center.mercator.x - dx;
		}
		else{
			vp_right = map_right;
			vp_top = map_top;
			vp_left = map_left;
			vp_bottom = map_bottom;
			
			vp_width = vp_right - vp_left;
			vp_height = vp_top - vp_bottom;
			merc_per_pixel = vp_width / screen_width;
			
			is_init = true;
		}
	}
	
	protected abstract void drawOverlays(GL10 gl);
	
	protected void updateMap(GL10 gl){		
		gl.glMatrixMode(GL10.GL_PROJECTION); 
		gl.glPushMatrix();

		updateZoom(gl);
		updatePan(gl);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		drawOverlays(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION); 
		gl.glPopMatrix();
	}
	
	private void updateZoom(GL10 gl){
		gl.glScalef(current_scale, current_scale, 1);
	}
	
	private void updatePan(GL10 gl){
		final float dx = MAP_CENTER.mercator.x / current_scale - vp_center.mercator.x;
		final float dy = MAP_CENTER.mercator.y / current_scale - vp_center.mercator.y;
		gl.glTranslatef(dx, dy, 0);
	}
	
	protected void zoom(PointF zoom_focus, float scale_factor){	
		final PointF center = getMercatorFromPixels(zoom_focus);
		vp_width = vp_width / scale_factor;
		vp_height = vp_height / scale_factor;
		current_scale *= scale_factor;
		merc_per_pixel = vp_width / screen_width;	
		newCenter(center);
		
		final float dx = zoom_focus.x - screen_width / 2.0f;
		final float dy = zoom_focus.y - screen_height / 2.0f;
		pan(-dx, -dy);
	}
	
	protected void pan(float dx, float dy){
		final float merc_dx = dx * merc_per_pixel;
		final float merc_dy = -dy * merc_per_pixel;
		vp_top += merc_dy;
		vp_bottom += merc_dy;
		vp_left += merc_dx;
		vp_right += merc_dx;
		vp_center.mercatorOffset(merc_dx, merc_dy);
	}
	
	protected void newCenter(LatLng center){
		newCenter(center.mercator);
	}
	
	protected void newCenter(PointF mercator){
		vp_center.set(mercator);
		vp_left = vp_center.mercator.x - vp_width / 2.0f;
		vp_right = vp_center.mercator.x + vp_width / 2.0f;
		vp_top = vp_center.mercator.y + vp_height / 2.0f;
		vp_bottom = vp_center.mercator.y - vp_height / 2.0f;
	}
	
	public LatLng getLatLngFromPixels(float x, float y){
		return new LatLng(getMercatorFromPixels(x,y));
	}
	
	public LatLng getLatLngFromPixels(PointF coords){
		return new LatLng(getMercatorFromPixels(coords));
	}
	
	protected PointF getMercatorFromPixels(float x, float y){
		final float merc_x = vp_left + x / screen_width * vp_width;
		final float merc_y = vp_top - y / screen_height * vp_height;
		return new PointF(merc_x, merc_y);
	}
	
	protected PointF getMercatorFromPixels(PointF coords){
		return getMercatorFromPixels(coords.x, coords.y);
	}
	
}
