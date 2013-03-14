package me.mattsutter.conditionred.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.mattsutter.conditionred.util.LatLng;
import me.mattsutter.conditionred.graphics.RenderCommand;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RadarRenderer implements Renderer {
	
	private final static int OVERLAY_DEFAULT_NUM = 3;
	
	private final ConcurrentLinkedQueue<RenderCommand> queue;
	private final Context context;
	private RenderCommand command;
	private final HashMap<String, LatLng> sites;
	private MapProjection projection = new MapProjection(){

		@Override
		protected void drawOverlays(GL10 gl) {
			for (Overlay overlay : overlays)
				overlay.draw(gl, projection);
		}
		
	};
	
	private final ArrayList<Overlay> overlays;
//	private boolean pan_in_progress = false;
//	private boolean zoom_in_progress = false;
		
	public RadarRenderer(Context context, ConcurrentLinkedQueue<RenderCommand> q, int frame_num,
			HashMap<String, LatLng> sites){
		queue = q;
		this.context = context;
		overlays = new ArrayList<Overlay>(OVERLAY_DEFAULT_NUM);
		this.sites = sites;
	}
	
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		checkForCommands();
		
		projection.updateMap(gl);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		projection.onSurfaceChanged(gl, width, height);
	}	

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0, 0, 0, 0);

		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_FLAT);
		
		overlays.add(new SitesOverlay(gl, context, sites));
	}
	
	
	private void checkForCommands(){
		command = queue.poll();
		while (command != null){
			switch (command.getType()){
			case ALPHA_CHANGE:
				Log.d("RadarRenderer", "Alpha change");
//				changeImageAlpha((AlphaChangeCommand) command);
				break;
			case ZOOM:
				setUpZoom((ZoomCommand) command);
				break;
			case PAN:
				setUpPan((PanCommand) command);
				break;
			case PRODUCT_CHANGE:
				Log.d("RadarRenderer", "Product change");
//				productChange((ProductChangeCommand) command);
				break;
			case NEW_FRAME:
				Log.d("RadarRenderer", "New Frame");
//				addFrame((NewFrameCommand) command);
				break;
			case FRAME_CHANGE:
//				Log.d("RadarRenderer", "Frame change");
//				frameChange((FrameChangeCommand) command);
				break;
			}
			
			command = queue.poll();
		}
	}
	
	private void setUpPan(PanCommand command){
		if (command.site_change){
			projection.newCenter(command.new_center);
//			deInitAnimation();
		}
		else
			projection.pan(command.dx, command.dy);
	}
	
	private void setUpZoom(ZoomCommand command){		
		projection.zoom(command.zoom_focus, command.scale_factor);
	}
	
}
