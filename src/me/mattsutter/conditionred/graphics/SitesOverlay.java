package me.mattsutter.conditionred.graphics;

import java.util.HashMap;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.android.texample.GLText;

import me.mattsutter.conditionred.util.LatLng;

public class SitesOverlay implements Overlay {
	private static final String FONT_FILE = "Roboto-Regular.ttf";
	private static final int FONT_SIZE = 14;
	private static final int FONT_PAD_X = 2;
	private static final int FONT_PAD_Y = 2;
	
	private final GLText gl_text;
	private final TextOverlay[] indicators;
	
	public SitesOverlay(GL10 gl, Context context, HashMap<String, LatLng> sites){
		indicators = new TextOverlay[sites.size()];

		// Create the GLText
		gl_text = new GLText(gl, context.getAssets());
		gl_text.load(FONT_FILE, FONT_SIZE, FONT_PAD_X, FONT_PAD_Y); 
		
		final String[] site_ids = new String[sites.size()];
		sites.keySet().toArray(site_ids);		

		for (int i = 0; i < site_ids.length; i++)
			indicators[i] = new TextOverlay(gl_text, site_ids[i], sites.get(site_ids[i]));
	}

	public void draw(GL10 gl, MapProjection projection) {
		for (TextOverlay overlay : indicators)
			overlay.draw(gl, projection);
	}

}
