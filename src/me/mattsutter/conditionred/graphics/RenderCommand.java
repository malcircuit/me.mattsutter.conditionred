package me.mattsutter.conditionred.graphics;

public interface RenderCommand{
	public static enum Type{ PRODUCT_CHANGE, NEW_FRAME, FRAME_CHANGE, ALPHA_CHANGE, ZOOM, PAN};
	
	public abstract RenderCommand.Type getType();
}
