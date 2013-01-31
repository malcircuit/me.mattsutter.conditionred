package me.mattsutter.conditionred;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.texample.GLText;

import me.mattsutter.conditionred.products.ColorPalettes;
import me.mattsutter.conditionred.products.EchoTopPaintArray;
import me.mattsutter.conditionred.products.EchoTopPalette;
import me.mattsutter.conditionred.products.LegacyPalette;
import me.mattsutter.conditionred.products.LegacySRVPaintArray;
import me.mattsutter.conditionred.products.OneHourRainPaintArray;
import me.mattsutter.conditionred.products.PaintArray;
import me.mattsutter.conditionred.products.Palette;
import me.mattsutter.conditionred.products.Palette.Type;
import me.mattsutter.conditionred.products.ReflPaintArray;
import me.mattsutter.conditionred.products.ReflPalette;
import me.mattsutter.conditionred.products.SWPaintArray;
import me.mattsutter.conditionred.products.SWPalette;
import me.mattsutter.conditionred.products.StormTotalPaintArray;
import me.mattsutter.conditionred.products.StormTotalPalette;
import me.mattsutter.conditionred.products.VILPaintArray;
import me.mattsutter.conditionred.products.VILPalette;
import me.mattsutter.conditionred.products.VelPaintArray;
import me.mattsutter.conditionred.products.VelPalette;
import me.mattsutter.conditionred.util.LatLng;
import me.mattsutter.conditionred.util.RenderCommand;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.mattsutter.conditionred.products.RadarProduct.*;

public class RadarRenderer implements Renderer {
	
	static{
		System.loadLibrary("conditionred");
	}
	
	private static final String FONT_FILE = "Roboto-Regular.ttf";
	private static final int FONT_SIZE = 14;
	private static final int FONT_PAD_X = 2;
	private static final int FONT_PAD_Y = 2;
	private static final float MAX_LONG = -220.0f;
	private static final float MIN_LONG = -60.0f;
	private static final float MAX_LAT = 67.0f;
	private static final float MIN_LAT = 10.0f;
	private static final float MAP_BOTTOM = (float)LatLng.getMercatorY(MIN_LAT);
	private static final float MAP_TOP = (float)LatLng.getMercatorY(MAX_LAT);
	private static final float MAP_RIGHT = (float)LatLng.getMercatorX(MIN_LONG);
	private static final float MAP_LEFT = (float)LatLng.getMercatorX(MAX_LONG);
	
	private static final float MAP_WIDTH = -MAP_LEFT + MAP_RIGHT;
	private static final float MAP_HEIGHT = MAP_TOP - MAP_BOTTOM;
	private static final float MAP_ASPECT = MAP_WIDTH/MAP_HEIGHT;
	
	private final HashMap<String, LatLng> sites;
	
	private LatLng radar_center;
	private short image_alpha = 200;
	private float radar_width = 0;
	private int screen_width = 0;
	private int screen_height = 0;
	private float screen_aspect = 0f;
	private PointF center = new PointF(0, 0);
	private RenderCommand command;
	private final int frame_num;
	private Palette color_palette;
	private int current_frame = -1;
	private GLText gl_text;
	private final Context context;
	
	private boolean is_init = false;
	
//	private final RenderCommandQueue map_queue, product_queue;
	private final ConcurrentLinkedQueue<RenderCommand> queue;
		
	public RadarRenderer(Context context, ConcurrentLinkedQueue<RenderCommand> q, int frame_num,
			HashMap<String, LatLng> sites){
		queue = q;
		this.frame_num = frame_num;
		this.context = context;
		this.sites = sites;
//		initVerts();
	}
	
	public void deInitAnimation(){
		if (is_init){
			current_frame = -1;
			DeInit();
			is_init = false;
		}
	}
//	
//	private void goToPoint(GL10 gl, PointF point){
//		gl.glTranslatef(point.x - center.x, point.y - center.y, 0);
//		center = point;
//	}
//	
//	private void setMapWidth(GL10 gl, float width){
//		gl.glScalef(radar_width/width, radar_width/width, 0);
//	}
	
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

//		gl.glMatrixMode(GL10.GL_PROJECTION); 
//		gl.glPushMatrix();
		
		//TODO: Do some zoom/pan stuff here.
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		drawSiteIndicators(gl, sites, screen_width);
		
//		checkForCommands();
//		
//		if (is_init){
//			gl.glTranslatef(center.x, center.y, 0);
//			gl.glScalef(radar_width/2, radar_width/2, 0.0f);
//
////			Log.d("RadarRenderer", "Drawing Frame: " + Integer.toString(current_frame));
//			bufferFrame(current_frame);
//			drawImage(current_frame);
//		}

//		gl.glMatrixMode(GL10.GL_PROJECTION); 
//		gl.glPopMatrix();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		screen_width = width;
		screen_height = height;
		screen_aspect = (float)screen_width/(float)screen_height;
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
		gl.glLoadIdentity();                        // reset the matrix to its default state
		
		gl.glOrthof(MAP_LEFT, MAP_RIGHT, MAP_BOTTOM, MAP_TOP, -1, 1);

		// So the map always has the same aspect ratio.
		if (screen_aspect < MAP_ASPECT)
			gl.glScalef(1, screen_aspect/MAP_ASPECT, 1);
		else if (screen_aspect > MAP_ASPECT)
			gl.glScalef(MAP_ASPECT/screen_aspect, 1, 1);
	}	

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0, 0, 0, 0);

		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_FLAT);

		// Create the GLText
		gl_text = new GLText(gl, context.getAssets());

		// Load the font from file (set size + padding), creates the texture
		// NOTE: after a successful call to this the font is ready for rendering!
		gl_text.load(FONT_FILE, FONT_SIZE, FONT_PAD_X, FONT_PAD_Y); 
	}
	
	private void checkForCommands(){
		command = queue.poll();
		while (command != null){
			switch (command.getType()){
			case ALPHA_CHANGE:
				Log.d("RadarRenderer", "Alpha change");
				changeImageAlpha((AlphaChangeCommand) command);
				break;
			case MAP_CHANGE:
//				Log.d("RadarRenderer", "Map change");
				mapChange((MapChangeCommand) command);
				break;
			case PRODUCT_CHANGE:
				Log.d("RadarRenderer", "Product change");
				productChange((ProductChangeCommand) command);
				break;
			case NEW_FRAME:
				Log.d("RadarRenderer", "New Frame");
				addFrame((NewFrameCommand) command);
				break;
			case FRAME_CHANGE:
//				Log.d("RadarRenderer", "Frame change");
				frameChange((FrameChangeCommand) command);
				break;
			}
			
			command = queue.poll();
		}
	}
	
	private void frameChange(FrameChangeCommand command){
		if (command.current_frame >= 0 && current_frame != command.current_frame){
			unbufferFrame(current_frame);
			current_frame = command.current_frame;
			bufferFrame(current_frame);
		}
	}
	
	private void drawSiteIndicators(GL10 gl, HashMap<String, LatLng> sites, int screen_width){
		gl.glEnable(GL10.GL_TEXTURE_2D);              // Enable Texture Mapping
		gl.glEnable(GL10.GL_BLEND);                   // Enable Alpha Blend
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);  // Set Alpha Blend Function
		
		final float scale = MAP_WIDTH/screen_width;
		final String[] site_ids = new String[sites.size()];
		sites.keySet().toArray(site_ids);

		PointF point;
		float x, y;
		
		// So we don't fuck up anything else we may need to draw on the map...
		gl.glPushMatrix();
		// So the font textures are rendered in screen pixels...
		gl.glScalef(scale, scale, 1);
		for (int i = 0; i < site_ids.length; i++){
			point = sites.get(site_ids[i]).toPixels();
			// So the markers are still drawn in the same relative locations...
			x = point.x / scale;
			y = point.y / scale;
			drawText(site_ids[i], x, y, 0);
		}
		gl.glPopMatrix();
		
		// disable texture + alpha
		gl.glDisable(GL10.GL_BLEND);                  // Disable Alpha Blend
		gl.glDisable(GL10.GL_TEXTURE_2D);             // Disable Texture Mapping
	}
	
	private void drawText(String text, float x, float y, float z){
		gl_text.begin(1.0f, 1.0f, 1.0f, 1.0f);
		gl_text.drawCY(text, x, y, z);
		gl_text.end();
	}
	
	private void mapChange(MapChangeCommand command){
		center = command.radar_center;
//		radar_width = command.radar_width;
		if (command.site_change)
			deInitAnimation();
	}
	
	private boolean changeImageAlpha(AlphaChangeCommand command){
		short alpha = command.image_alpha;
		if (alpha != image_alpha){
			changeAlpha(alpha);
			image_alpha = alpha;
			return true;
		}
		return false;
	}
	
	private void productChange(ProductChangeCommand command){
		deInitAnimation();
		
		switch(command.prod_code){
		//case BASE_REFL:
		//case BASE_REFL_LONG:
		case E_BASE_REFL:
		case D_HYB_SCAN_REFL:
			color_palette = new ReflPalette();
			break;
		//case BASE_VEL:
		case E_BASE_VEL:
			color_palette = new VelPalette();
			break;
		case SRV:
//			PAINTS = new SRVPaintArray(product);
			color_palette = new LegacyPalette(Type.SRV);
			break;
		case BASE_SW_SHORT:
		case BASE_SW_LONG:
			color_palette = new SWPalette();
			break;
		case RAIN_TOTAL_1HR:
		case RAIN_TOTAL_3HR:
			color_palette = new LegacyPalette(Type.ONE_HR_RAIN);
			break;
		//case RAIN_TOTAL_STRM:
		case D_RAIN_TOTAL_STRM:
			color_palette = new StormTotalPalette();
			break;
		case E_ECHO_TOPS:
			color_palette = new EchoTopPalette();
			break;
		case D_VIL:
			color_palette = new VILPalette();
			break;
		default:
			color_palette = new ReflPalette();
		}
		
		ColorPalettes.init(color_palette);
	}
	

	public void addFrame(NewFrameCommand command){
			if (!is_init){
				initAnimation(command.bin_num, frame_num);
				is_init = true;
			}

			PaintArray paints;
			switch(color_palette.getType()){
			case VEL:
				paints = new VelPaintArray((VelPalette) color_palette, command.thresh);
				break;
			case SRV:
				paints = new LegacySRVPaintArray((LegacyPalette) color_palette, command.thresh);
				break;
			case SW:
				paints = new SWPaintArray((SWPalette) color_palette);
				break;
			case ONE_HR_RAIN:
				paints = new OneHourRainPaintArray((LegacyPalette) color_palette, command.thresh);
				break;
			case STRM_TOTAL:
				paints = new StormTotalPaintArray((StormTotalPalette) color_palette, command.thresh);
				break;
			case ET:
				paints = new EchoTopPaintArray((EchoTopPalette) color_palette);
				break;
			case VIL:
				paints = new VILPaintArray((VILPalette) color_palette, command.thresh);
				break;
			default:
				paints = new ReflPaintArray((ReflPalette) color_palette, command.thresh);
				break;
			}

			addFrame(paints, command.rle, command.frame_index);
			
			if (current_frame < 0)
				current_frame = command.frame_index;
	}
	
	private static native void initAnimation(int radial_bin_num, int num_frames);
	private static native void DeInit();
	private static native void drawImage(int index);
	private static native void bufferFrame(int index);
	private static native void unbufferFrame(int index);
	private static native void changeAlpha(short new_alpha);
	private static native void addFrame(PaintArray paint_array, byte[][] rle, int index);
}
