package me.mattsutter.conditionred.util;

public interface RenderCommand{
	public static enum Type{ PRODUCT_CHANGE, NEW_FRAME, FRAME_CHANGE, ALPHA_CHANGE, MAP_CHANGE };
	
	public abstract RenderCommand.Type getType();
	public abstract long getTimeSent();
}
