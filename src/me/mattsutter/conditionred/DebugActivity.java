package me.mattsutter.conditionred;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.os.Bundle;
//import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class DebugActivity extends MapActivity implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);
		
        CustomMapView map_view = (CustomMapView) findViewById(R.id.map_view);
        map_view.setBuiltInZoomControls(true);
        map_view.setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_NEVER);

//		RadarView radar_view = new RadarView(this.getApplicationContext(), map_view);
//		
//		map_view.addView(radar_view, new MapView.LayoutParams(
//				MapView.LayoutParams.FILL_PARENT, 
//				MapView.LayoutParams.FILL_PARENT, 
//				0,0, 
//				MapView.LayoutParams.TOP_LEFT)
//		);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onDoubleTap(MotionEvent e) {
		//((CustomMapView)findViewById(R.id.custom_map_view)).getController().zoomInFixing((int)e.getX(), (int)e.getY());
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
