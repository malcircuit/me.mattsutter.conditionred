package me.mattsutter.conditionred;

import java.util.concurrent.ConcurrentLinkedQueue;

import me.mattsutter.conditionred.util.RenderCommand;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class RadarView extends GLSurfaceView {

	protected final RadarRenderer renderer;
//	private final RenderCommandQueue queue = new RenderCommandQueue();
//	private int prod_code;

	public RadarView(Context context, ConcurrentLinkedQueue<RenderCommand> queue, int frame_num) {
		super(context);
		renderer = new RadarRenderer(queue, frame_num);

		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		setRenderer(renderer);
//		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setZOrderOnTop(true);
	}

//	public void newRadarImage(RadialProduct product){
////		if (product != null){
////			prod_code = product.prod_type;
////			mapHasChanged(product.prod_block.radar_center);
////			return queue.sendProductChange(product);
////		}
////		return false;
//		
//		if (product != null){
//			prod_code = product.prod_type;
//			mapHasChanged(product.prod_block.radar_center);
//			queue.add(new RenderCommand(product));
//		}
//	}
//	
//	protected void mapHasChanged(GeoPoint radar_center){
////		return queue.sendMapUpdate(map_view, prod_code, radar_center);
//		queue.add(new RenderCommand(map_view, prod_code, radar_center));
//	}
//	
//	protected void changeAlpha(short alpha){
////		return queue.sendAlphaChange(alpha);
//		queue.add(new RenderCommand(alpha));
//	}
	
	public void onDestroy(){
		renderer.deInitAnimation();
	}
}
