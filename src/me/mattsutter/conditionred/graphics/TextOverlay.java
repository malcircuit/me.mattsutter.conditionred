package me.mattsutter.conditionred.graphics;

import javax.microedition.khronos.opengles.GL10;
import me.mattsutter.conditionred.util.LatLng;
import com.android.texample.GLText;

public class TextOverlay implements Overlay {	
	private final GLText gl_text;
	private final String text;
	private final LatLng coords;

	public TextOverlay(GLText gl_text, String text, LatLng coords){
		this.gl_text = gl_text;
		this.text = text;
		this.coords = coords;
	}

	public void draw(GL10 gl, MapProjection projection) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		// So we don't fuck up anything else we may need to draw on the map...
		gl.glPushMatrix();
		// So the font textures are rendered in screen pixels...
		gl.glScalef(projection.merc_per_pixel, projection.merc_per_pixel, 1);

		// So the markers are still drawn in the same relative locations...
		final float x = coords.mercator.x / projection.merc_per_pixel;
		final float y = coords.mercator.y / projection.merc_per_pixel;
		gl_text.begin(1.0f, 1.0f, 1.0f, 1.0f);
		gl_text.drawCY(text, x, y, 0);
		gl_text.end();

		gl.glPopMatrix();
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

}
