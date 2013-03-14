package me.mattsutter.conditionred.graphics;

import me.mattsutter.conditionred.graphics.RenderCommand;

public class AlphaChangeCommand implements RenderCommand {

	public final short image_alpha;
	
	public AlphaChangeCommand(short new_alpha){
		assert new_alpha < 256 && new_alpha >= 0: "Bad alpha value.";
		image_alpha = new_alpha;
	}
	
	public Type getType() {
		return Type.ALPHA_CHANGE;
	}

}
