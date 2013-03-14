package me.mattsutter.conditionred.graphics;

import android.graphics.PointF;
import me.mattsutter.conditionred.graphics.RenderCommand;

public class ZoomCommand implements RenderCommand {

	private static final float DOUBLE_TAP_SCALE = 2.0f;
	
	public final PointF zoom_focus;
	public final float scale_factor;
//	public final ZoomStage stage;
	
	public ZoomCommand(PointF center, float scale){
		zoom_focus = center;
		scale_factor = scale;
	}
	
	public ZoomCommand(float x, float y, float scale){
		zoom_focus = new PointF(x, y);
		scale_factor = scale;
	}
	
	public ZoomCommand(float x, float y){
		zoom_focus = new PointF(x, y);
		scale_factor = DOUBLE_TAP_SCALE;
	}

	public Type getType() {
		return Type.ZOOM;
	}
}
