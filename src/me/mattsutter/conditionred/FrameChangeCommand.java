package me.mattsutter.conditionred;

import me.mattsutter.conditionred.util.RenderCommand;

public class FrameChangeCommand implements RenderCommand {

	public final int current_frame;
	private final long time_sent;
	
	public FrameChangeCommand(int frame_index){
		current_frame = frame_index;
		time_sent = System.currentTimeMillis();
	}
	
	public Type getType() {
		return Type.FRAME_CHANGE;
	}

	public long getTimeSent() {
		return time_sent;
	}

}
