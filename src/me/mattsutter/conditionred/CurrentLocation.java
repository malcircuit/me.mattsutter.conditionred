package me.mattsutter.conditionred;

import me.mattsutter.conditionred.util.DatabaseQuery;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import static me.mattsutter.conditionred.CustomMapView.*;
/** 
 * FIX ME!
 * @author Matt Sutter
 */
public class CurrentLocation extends MyLocationOverlay
{
	private static final int[] PRODUCT_RADII = {SHORT_DIAM/(METERS_PER_NMI * 120) * 1000, 
												DIAMETER/(METERS_PER_NMI * 120) * 1000,
												VEL_DIAM/(METERS_PER_NMI * 120) * 1000,
												ECHO_TOP_DIAM/(METERS_PER_NMI * 120) * 1000,
												LONG_DIAM/(METERS_PER_NMI * 120) * 1000};
	
	public CurrentLocation(Context context, MapView map_view){
		super(context, map_view);
		this.enableMyLocation();
	}
	
	protected String getClosestSite(){
		String site = "";
		GeoPoint location = this.getMyLocation();

		if (location != null){
			int lat_upper = (int)(location.getLatitudeE6()/1000);
//			int lat_upper = -90399;
			int lat_lower = lat_upper;
			int long_upper = (int)(location.getLongitudeE6()/1000);
//			int long_upper = 40859;
			int long_lower = long_upper;

			Cursor site_list = DatabaseQuery.getSitesBetween(lat_lower - PRODUCT_RADII[0], lat_upper + PRODUCT_RADII[0],
					long_lower - PRODUCT_RADII[0], long_upper + PRODUCT_RADII[0]);

			for (int radius : PRODUCT_RADII){
				site_list = DatabaseQuery.getSitesBetween(lat_lower - radius, lat_upper + radius,
						long_lower - radius, long_upper + radius);
				if (site_list.getCount() >= 1)
					break;
			}

			if (site_list.getCount() == 1){
				if (site_list.moveToFirst()){
					return site_list.getString(0);
				}
			}
			else if (site_list.getCount() > 1)
				return closestSite(site_list, location);
		}
		
		return site;
	}
	
	private String closestSite(Cursor cursor, GeoPoint location){
		int[] coords = new int[2];
		float[] results = new float[1];
		int closest_site = 0;
		
		cursor.moveToFirst();
		
		coords = DatabaseQuery.getLatLong(cursor.getString(0));
		Location.distanceBetween(location.getLatitudeE6()/1E6, location.getLongitudeE6()/1E6, 
								 coords[0]/1000.0d, coords[1]/1000.0d, results);

		float distance = results[0];
		
		for (int i = 1; i < cursor.getCount(); i++){
			coords = DatabaseQuery.getLatLong(cursor.getString(0));
			Location.distanceBetween(location.getLatitudeE6()/1E6, location.getLongitudeE6()/1E6, 
									 coords[0]/1000.0d, coords[1]/1000.0d, results);
			
			if (distance > results[0]){
				distance = results[0];
				closest_site = i;
			}
			
			cursor.moveToNext();
		}
		
		cursor.moveToPosition(closest_site);
		
		return cursor.getString(0);
	}    
	
	@Override
	public boolean draw(Canvas canvas, MapView map_view, boolean shadow, long when){
		return super.draw(canvas, map_view, shadow, when);
	}
}
