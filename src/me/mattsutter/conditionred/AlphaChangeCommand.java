package me.mattsutter.conditionred;

import me.mattsutter.conditionred.util.RenderCommand;

public class AlphaChangeCommand implements RenderCommand {

	private final long time_sent;
	public final short image_alpha;
	
	public AlphaChangeCommand(short new_alpha){
		assert new_alpha < 256 && new_alpha >= 0: "Bad alpha value.";
		image_alpha = new_alpha;
		time_sent = System.currentTimeMillis();
	}
	
	public Type getType() {
		return Type.ALPHA_CHANGE;
	}

	public long getTimeSent() {
		return time_sent;
	}

}
