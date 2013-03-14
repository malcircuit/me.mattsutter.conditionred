package me.mattsutter.conditionred.graphics;

import me.mattsutter.conditionred.products.RadarProduct;
import me.mattsutter.conditionred.graphics.RenderCommand;

public class ProductChangeCommand implements RenderCommand {

	public final int prod_code;
	
	public ProductChangeCommand(RadarProduct product){
		prod_code = product.prod_code;
	}
	
	public ProductChangeCommand(int prod_code){
		this.prod_code = prod_code;
	}
	
	public Type getType() {
		return Type.PRODUCT_CHANGE;
	}

}
