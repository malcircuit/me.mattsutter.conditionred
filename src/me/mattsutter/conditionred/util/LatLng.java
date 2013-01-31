package me.mattsutter.conditionred.util;

import java.lang.Math;

import android.graphics.PointF;

public class LatLng {

	// This was in the wikipedia entry for Mercator projection.  
	// We'll see if we actually need it later.
	private static final double RADIUS = 1d; 
	
	private static final float FIXED_POINT_DIVISOR = 1000f;
	private final double lat;
	private final double lng;

	/**
	 * LatLng constructor. 
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public LatLng(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * LatLng constructor. 
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public LatLng(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * Helper for loading radar sites from the database. The latitude and longitude values 
	 * stored in the database are fixed-point integers (three fractional digits).
	 * @param lat - latitude
	 * @param lng - longitude 
	 * @return new LatLng object.
	 */
	public static LatLng fromFixedPointInt(int lat, int lng){
		return new LatLng(lat / FIXED_POINT_DIVISOR, lng / FIXED_POINT_DIVISOR);
	}
	
	/**
	 * Converts the latitude into a x-coordinate for displaying on a
	 * Mercator projection map.
	 * @param long_in_deg - longitude value in degrees
	 * @return x value
	 */
	public static double getMercatorX(double long_in_deg){
		if (long_in_deg <= 0)
			return RADIUS * Math.toRadians(long_in_deg);
		return RADIUS * Math.toRadians(long_in_deg - 360d);
	}
	/**
	 * Converts the longitude into a y-coordinate for displaying on a
	 * Mercator projection map.
	 * @param lat_in_deg - latitude value in degrees
	 * @return y value
	 */
	public static double getMercatorY(double lat_in_deg){
		return RADIUS * Math.log(Math.tan(Math.PI/4 + Math.toRadians(lat_in_deg)/2));
	}
	
	/**
	 * Converts latitude and longitude coordinates into a {@link PointF} 
	 * with Mercator coordinates.
	 * @return {@link PointF} object with Mercator coordinates.
	 */
	public PointF toPixels(){
		return new PointF((float)getMercatorX(lng), (float)getMercatorY(lat));
	}
}
