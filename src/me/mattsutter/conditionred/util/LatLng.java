package me.mattsutter.conditionred.util;

import java.lang.Math;

import android.graphics.PointF;

/**
 * Class for storing latitude/longitude coordinates.
 * @author Matt Sutter
 *
 */
public class LatLng {

	// This was in the wikipedia entry for Mercator projection.  
	// We'll see if we actually need it later.
	private static final double RADIUS = 1d; 
	
	private static final float FIXED_POINT_DIVISOR = 1000f;
	public double lat;
	public double lng;
	public PointF mercator;

	/**
	 * {@link LatLng} constructor. 
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public LatLng(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
		mercator = toMercator(lat, lng);
	}
	
	/**
	 * {@link LatLng} constructor. 
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public LatLng(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
		mercator = toMercator(lat, lng);
	}
	
	/**
	 * Construct a new {@link LatLng} object from an existing one.
	 */
	public LatLng(LatLng coords){
		this.lat = coords.lat;
		this.lng = coords.lng;
		this.mercator = new PointF(coords.mercator.x, coords.mercator.y);
	}
	
	public LatLng(PointF mercator){
		this.mercator = new PointF(mercator.x, mercator.y);
		lat = getLatFromMercator(mercator.y);
		lng = getLongFromMercator(mercator.x);
	}
	
	/**
	 * Move the coordinate pair by some offset.
	 */
	public void offset(double lat_offset, double lng_offset){
		lat += lat_offset;
		lng += lng_offset;
		mercator = toMercator(lat, lng);
	}
	
	/**
	 * Move the coordinate pair by some offset.
	 */
	public void offset(float lat_offset, float lng_offset){
		lat += lat_offset;
		lng += lng_offset;
		mercator = toMercator(lat, lng);
	}
	
	public void mercatorOffset(float merc_x, float merc_y){
		mercator.offset(merc_x, merc_y);
		lat = getLatFromMercator(mercator.y);
		lng = getLongFromMercator(mercator.x);
	}
	
	/**
	 * Set new latitude/longitude coordinates.
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public void set(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
		mercator = toMercator(lat, lng);
	}
	
	/**
	 * Set new latitude/longitude coordinates.
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public void set(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
		mercator = toMercator(lat, lng);
	}
	
	/**
	 * Set new latitude/longitude coordinates from an existing {@link LatLng}.
	 * @param lat - latitude
	 * @param lng - longitude
	 */
	public void set(LatLng coords){
		mercator = new PointF(coords.mercator.x, coords.mercator.y);
		lat = coords.lat;
		lng = coords.lng;
	}
	
	public void set(PointF mercator){
		this.mercator = new PointF(mercator.x, mercator.y);
		lat = getLatFromMercator(mercator.y);
		lng = getLongFromMercator(mercator.x);
	}
	
	/**
	 * Helper for loading radar sites from the database. The latitude and longitude values 
	 * stored in the database are fixed-point integers (three fractional digits).
	 * @param lat - latitude
	 * @param lng - longitude 
	 * @return new {@link LatLng} object.
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
		return RADIUS * Math.log(Math.tan(Math.PI/4.0d + Math.toRadians(lat_in_deg)/2.0d));
	}
	
	public static double getLatFromMercator(double merc_y){
		return Math.toDegrees(2.0d * Math.atan(Math.exp(merc_y / RADIUS)) - Math.PI/2.0d);
	}
	
	public static double getLongFromMercator(double merc_x){
		return Math.toDegrees(merc_x / RADIUS);
	}
	/**
	 * Converts latitude and longitude coordinates into a {@link PointF} 
	 * with Mercator coordinates.
	 * @return {@link PointF} object with Mercator coordinates.
	 */
	public static PointF toMercator(double lat, double lng){
		return new PointF((float)getMercatorX(lng), (float)getMercatorY(lat));
	}
}
