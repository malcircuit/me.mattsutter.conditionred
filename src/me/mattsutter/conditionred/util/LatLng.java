package me.mattsutter.conditionred.util;

import java.lang.Math;

public class LatLng {

	private static final double RADIUS = 3000; //Made this up.  Please change
	private final double lat;
	private final double lng;

	public LatLng(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	public LatLng(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	public double getX(){
		return lng;
	}
	
	public double getY(){
		return RADIUS * Math.log(Math.tan(Math.PI/4 + Math.toRadians(lat)/2));
	}
}
