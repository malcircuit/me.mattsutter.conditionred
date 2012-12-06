package me.mattsutter.conditionred;

import static me.mattsutter.conditionred.products.RadarProduct.BASE_SW_SHORT;
import static me.mattsutter.conditionred.products.RadarProduct.D_VIL;
import static me.mattsutter.conditionred.products.RadarProduct.E_BASE_REFL;
import static me.mattsutter.conditionred.products.RadarProduct.E_BASE_VEL;
import static me.mattsutter.conditionred.products.RadarProduct.E_ECHO_TOPS;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class CustomMapView extends MapView {
	
	public static final int METERS_PER_NMI = 1852;
	//public static final float RADAR_MAP_D = 248/60;

	// Possible radii for radial products in meters.
	public static final int SHORT_DIAM = 64 * METERS_PER_NMI;
	public static final int DIAMETER = 248 * METERS_PER_NMI;
	public static final int LONG_DIAM = 496 * METERS_PER_NMI;
	public static final int VEL_DIAM = 324 * METERS_PER_NMI;
	public static final int ECHO_TOP_DIAM = 372 * METERS_PER_NMI;
	
	private GestureDetector gest_detect;
	
	private Runnable progOn, progOff;
	private Handler handler;
	private boolean progress = false;

	public CustomMapView(Context context, String apiKey) {
		super(context, apiKey);
		init(context);
	}

	public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public CustomMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		gest_detect = new GestureDetector(context, (GestureDetector.OnGestureListener) context);
		gest_detect.setOnDoubleTapListener((GestureDetector.OnDoubleTapListener) context);
        setBuiltInZoomControls(true);
        setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_NEVER);
        setWillNotDraw(false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e){
		if (gest_detect.onTouchEvent(e))
			return true;
		return super.onTouchEvent(e);
	}

	protected void setProgRunners(Handler hand, Runnable progOn, Runnable progOff){
		this.progOn = progOn;
		this.progOff = progOff;
		handler = hand;
	}
	
	public void progressOn(){
		if (progOn != null)
			handler.post(progOn);
	}
	
	public void progressOff(){
		if (progOff != null)
			handler.post(progOff);
	}
	
	protected boolean toggleProgress(){
		if (progress){
			progress = false;
			progressOff();
		}
		else{
			progress = true;
			progressOn();
		}
		
		return progress;
	}
	
	/**
	 * Computes the width (in pixels) of the radar image based on the current projection
	 * of the MapView.
	 * @param map_view - MapView to get the projection of.
	 */
	public static float getRadarWidth(MapView map_view, int prod_type, GeoPoint radar_center){
		switch(prod_type){
		case E_BASE_REFL:
		case D_VIL:
			return (float) (map_view.getProjection().metersToEquatorPixels(LONG_DIAM) 
					* (1 / Math.cos(Math.toRadians(radar_center.getLatitudeE6() / 1E6)) )) ;
		case E_BASE_VEL:
		//case SRV:
			return(float) (map_view.getProjection().metersToEquatorPixels(VEL_DIAM) 
					* (1 / Math.cos(Math.toRadians(radar_center.getLatitudeE6() / 1E6)) ) );
		case E_ECHO_TOPS:
			return (float) (map_view.getProjection().metersToEquatorPixels(ECHO_TOP_DIAM) 
					* (1 / Math.cos(Math.toRadians(radar_center.getLatitudeE6() / 1E6)) ) );
		case BASE_SW_SHORT:
			return (float) (map_view.getProjection().metersToEquatorPixels(SHORT_DIAM) 
					* (1 / Math.cos(Math.toRadians(radar_center.getLatitudeE6() / 1E6)) ) );
		default:
			// Don't even ask me how this works, I just copied it from somewhere on the internet. 
			return (float) (map_view.getProjection().metersToEquatorPixels(DIAMETER) 
						* (1 / Math.cos(Math.toRadians(radar_center.getLatitudeE6() / 1E6)) ) );
		}
    }
}
