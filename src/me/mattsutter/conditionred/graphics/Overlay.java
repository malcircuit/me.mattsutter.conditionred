package me.mattsutter.conditionred.graphics;

import javax.microedition.khronos.opengles.GL10;

public interface Overlay {

	void draw(GL10 gl, MapProjection projection);
}
