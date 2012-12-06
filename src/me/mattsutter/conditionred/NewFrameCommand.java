package me.mattsutter.conditionred;

import me.mattsutter.conditionred.products.RadarProduct;
import me.mattsutter.conditionred.util.RenderCommand;

public class NewFrameCommand implements RenderCommand {

	private final long time_sent;
	
	public final int prod_code;
	public final RadarProduct.Type type;
	public final byte[][] rle;
	public final int[] thresh;
	public final int p8;
	public final int p9;
	public final int bin_num;
	public final long exp_date;
	public final int frame_index;
	
	
	public NewFrameCommand(RadarProduct product, int frame_index){
		type = product.type;
		prod_code = product.prod_code;
		time_sent = System.currentTimeMillis();
		exp_date = product.getExpirationDate();
		this.frame_index = frame_index;
		rle = product.radial_layer.rle;
		thresh = product.prod_block.thresh;
		p8 = product.prod_block.p[7];
		p9 = product.prod_block.p[8];
		bin_num = product.radial_layer.range_bin_num;
	}

	public NewFrameCommand(int prod_code, long exp_date,
			byte[][] rle, int[] thresh, int p8, int p9, int rad_bin_num, int frame_index){
		this.type = RadarProduct.Type.RADIAL;
		this.prod_code = prod_code;
		time_sent = System.currentTimeMillis();
		this.exp_date = exp_date;
		this.rle = rle;
		this.thresh = thresh;
		this.p8 = p8;
		this.p9 = p9;
		bin_num = rad_bin_num;
		this.frame_index = frame_index;
	}

	public Type getType() {
		return Type.NEW_FRAME;
	}

	public long getTimeSent() {
		return time_sent;
	}


}
