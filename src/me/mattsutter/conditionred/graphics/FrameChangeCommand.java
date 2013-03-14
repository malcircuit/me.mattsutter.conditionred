package me.mattsutter.conditionred.graphics;

import me.mattsutter.conditionred.graphics.RenderCommand;

public class FrameChangeCommand implements RenderCommand {

	public final int current_frame;
	
	public FrameChangeCommand(int frame_index){
		current_frame = frame_index;
	}
	
	public Type getType() {
		return Type.FRAME_CHANGE;
	}

}
