/**
 * 
 */
package me.mattsutter.conditionred.graphics;

import static me.mattsutter.conditionred.products.RadarProduct.BASE_SW_LONG;
import static me.mattsutter.conditionred.products.RadarProduct.BASE_SW_SHORT;
import static me.mattsutter.conditionred.products.RadarProduct.D_HYB_SCAN_REFL;
import static me.mattsutter.conditionred.products.RadarProduct.D_RAIN_TOTAL_STRM;
import static me.mattsutter.conditionred.products.RadarProduct.D_VIL;
import static me.mattsutter.conditionred.products.RadarProduct.E_BASE_REFL;
import static me.mattsutter.conditionred.products.RadarProduct.E_BASE_VEL;
import static me.mattsutter.conditionred.products.RadarProduct.E_ECHO_TOPS;
import static me.mattsutter.conditionred.products.RadarProduct.RAIN_TOTAL_1HR;
import static me.mattsutter.conditionred.products.RadarProduct.RAIN_TOTAL_3HR;
import static me.mattsutter.conditionred.products.RadarProduct.SRV;
import static me.mattsutter.conditionred.products.RadarProduct.VIL;

import javax.microedition.khronos.opengles.GL10;

import me.mattsutter.conditionred.products.ColorPalettes;
import me.mattsutter.conditionred.products.EchoTopPaintArray;
import me.mattsutter.conditionred.products.EchoTopPalette;
import me.mattsutter.conditionred.products.LegacyPalette;
import me.mattsutter.conditionred.products.LegacySRVPaintArray;
import me.mattsutter.conditionred.products.OneHourRainPaintArray;
import me.mattsutter.conditionred.products.PaintArray;
import me.mattsutter.conditionred.products.Palette;
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
import me.mattsutter.conditionred.products.Palette.Type;

/**
 * @author matt
 *
 */
public class AnimationOverlay implements Overlay {

	static{
		System.loadLibrary("conditionred");
	}

	private final int frame_num;
		
	private Palette color_palette;
	private short image_alpha = 200;
	private int current_frame = -1;
	private boolean is_init = false;
	
	public AnimationOverlay(int frame_num){
		this.frame_num = frame_num;
	}
	
	protected void productChange(ProductChangeCommand command){
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

	protected void addFrame(NewFrameCommand command){
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
	
	protected void frameChange(FrameChangeCommand command){
		if (command.current_frame >= 0 && current_frame != command.current_frame){
			unbufferFrame(current_frame);
			current_frame = command.current_frame;
			bufferFrame(current_frame);
		}
	}
		
	protected boolean changeImageAlpha(AlphaChangeCommand command){
		final short alpha = command.image_alpha;
		if (alpha != image_alpha){
			changeAlpha(alpha);
			image_alpha = alpha;
			return true;
		}
		return false;
	}

	protected void deInitAnimation(){
		if (is_init){
			current_frame = -1;
			DeInit();
			is_init = false;
		}
	}
	
	public void draw(GL10 gl, MapProjection projection) {
//		if (is_init){
//			gl.glTranslatef(center.x, center.y, 0);
//			gl.glScalef(radar_width/2, radar_width/2, 0.0f);
//
////			Log.d("RadarRenderer", "Drawing Frame: " + Integer.toString(current_frame));
//			bufferFrame(current_frame);
//			drawImage(current_frame);
//		}
	}
	
	private static native void initAnimation(int radial_bin_num, int num_frames);
	private static native void DeInit();
	private static native void drawImage(int index);
	private static native void bufferFrame(int index);
	private static native void unbufferFrame(int index);
	private static native void changeAlpha(short new_alpha);
	private static native void addFrame(PaintArray paint_array, byte[][] rle, int index);
}
