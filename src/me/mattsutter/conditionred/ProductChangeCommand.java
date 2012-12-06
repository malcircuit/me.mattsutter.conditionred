package me.mattsutter.conditionred;

import me.mattsutter.conditionred.products.RadarProduct;
import me.mattsutter.conditionred.util.RenderCommand;

public class ProductChangeCommand implements RenderCommand {

	public final int prod_code;
	private final long time_sent;
	
	public ProductChangeCommand(RadarProduct product){
		prod_code = product.prod_code;
		time_sent = System.currentTimeMillis();
	}
	
	public ProductChangeCommand(int prod_code){
		this.prod_code = prod_code;
		time_sent = System.currentTimeMillis();
	}
	
	public Type getType() {
		return Type.PRODUCT_CHANGE;
	}

	public long getTimeSent() {
		return time_sent;
	}

}
