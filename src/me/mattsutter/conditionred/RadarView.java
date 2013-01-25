package me.mattsutter.conditionred;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class CustomMapView extends View {
	
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

	public CustomMapView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		gest_detect = new GestureDetector(context, (GestureDetector.OnGestureListener) context);
		gest_detect.setOnDoubleTapListener((GestureDetector.OnDoubleTapListener) context);
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
}
