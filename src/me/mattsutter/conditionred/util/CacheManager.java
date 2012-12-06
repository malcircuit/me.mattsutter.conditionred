package me.mattsutter.conditionred.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import me.mattsutter.conditionred.products.RadarProduct;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
//import android.util.Log;

import static me.mattsutter.conditionred.util.CacheDBHelper.*;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_ID;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.PROD_ANGLE;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.PROD_TYPE;
import static me.mattsutter.conditionred.products.ProductDescriptionBlock.MJDtoCal;
import static me.mattsutter.conditionred.products.ProductDescriptionBlock.secToHour;
import static me.mattsutter.conditionred.products.RadarProduct.MAX_SCAN_NUM;

//TODO: Add function to clear entire cache folder and database.
public class CacheManager {
	public static final String NO_CACHE_ERROR = "No cache file to load";
	public static final String CACHE_EXPIRED = "Cache file has expired";
	
	private static final int MAX_CACHED = 10;
	private static final long TIME_LIMIT = 24 * 60 * 60 * 1000;
	protected volatile boolean is_loaded = false;
	private SQLiteDatabase cache_db;
	private CacheDBHelper cache_helper;
	private Context context;
	
	public CacheManager(Context context){
		this.context = context;
		open();
	}
	
	public synchronized void register(RadarProduct product){
		ContentValues values = new ContentValues();
		values.put(SITE_ID, product.site);
		values.put(PROD_TYPE, product.prod_code);
		values.put(PROD_ANGLE, DatabaseQuery.getProductAngleFromURL(product.url));
		values.put(SCAN_NUM, product.prod_block.vol_scan_num);
		values.put(EXPIRATION_DATE, product.getExpirationDate());
		values.put(IS_EXPIRED, FALSE);

		if (is_loaded)
			cache_db.insert(CACHE_TABLE, null, values);
	}
//	
//	public synchronized void register(String site, int prod_type, int prod_angle, int date, int time){
//		ContentValues values = new ContentValues();
//		values.put(SITE_ID, site);
//		values.put(PROD_TYPE, prod_type);
//		values.put(PROD_ANGLE, prod_angle);
//		values.put(SCAN_DATE, getTimeCreatedInMillis(date, time));
//		values.put(IS_EXPIRED, FALSE);
//		
//		if (is_loaded)
//			cache_db.insert(CACHE_TABLE, null, values);
//	}
	
	public synchronized void tagExpired(Cursor old_cache_files, int _id_column){
		int index = old_cache_files.getInt(_id_column);
		String where = "_id=" + Integer.toString(index);
		ContentValues values = new ContentValues();
		values.put(IS_EXPIRED, TRUE);
		
		if (is_loaded)
			cache_db.update(CACHE_TABLE, values, where, null);
	}
	
	public synchronized void checkForExpired(String site, int seq_start){
		String query = 	"SELECT " + EXPIRATION_DATE + ", _id FROM " 
						+ CACHE_TABLE + " WHERE "  
						+ SITE_ID + "=\""  + site + "\""
						+ " AND " + EXPIRATION_DATE + " < " 
						+ Long.toString(System.currentTimeMillis() - TIME_LIMIT)
						+ " ORDER BY " + EXPIRATION_DATE + " DESC";

		if (is_loaded){
			Cursor result = cache_db.rawQuery(query, null);

			if (result.moveToFirst() && result.getCount() > 1){
				result.moveToNext();
				for (int i = 1; i < result.getCount(); i++){
					tagExpired(result, result.getColumnCount() - 1);
					result.moveToNext();
				}
			}
			
			int seq_end = (seq_start - MAX_CACHED + MAX_SCAN_NUM) % MAX_SCAN_NUM;
			assert(seq_start != seq_end);
			
			if (seq_start < seq_end){
				query = 	"SELECT _id FROM " 
							+ CACHE_TABLE + " WHERE "  
							+ SITE_ID + "=\""  + site + "\""
							+ " AND " + SCAN_NUM + "NOT BETWEEN " 
							+ Integer.toString(seq_start) + " AND " + Integer.toString(seq_end);
			}
			else{
				query = 	"SELECT _id FROM " 
							+ CACHE_TABLE + " WHERE "  
							+ SITE_ID + "=\""  + site + "\""
							+ " AND " + SCAN_NUM + " BETWEEN " 
							+ Integer.toString(seq_start) + " AND " + Integer.toString(seq_end);
			}
			
			result = cache_db.rawQuery(query, null);

			if (result.moveToFirst() && result.getCount() > 1){
				result.moveToNext();
				for (int i = 1; i < result.getCount(); i++){
					tagExpired(result, result.getColumnCount() - 1);
					result.moveToNext();
				}
			}
			result.close();
		}
	}
	
	public synchronized void flushCache(){
		String sql = "SELECT _id FROM " + CACHE_TABLE;
		
		if (is_loaded){
			Cursor result = cache_db.rawQuery(sql, null);
			if (result.moveToFirst() && result.getCount() > 0){
				for (int i = 0; i < result.getCount(); i++){
					tagExpired(result, 0);
					result.moveToNext();
				}
			}
			
			result.close();
			
			removeExpired();
		}
	}
	
	public synchronized void removeExpired(){
		final String[] expired;
		final int[] id_array;
		String query = 	"SELECT " + SITE_ID + ", "
						+ PROD_TYPE + ", "
						+ PROD_ANGLE + ", "
						+ SCAN_NUM + ", "
						+ EXPIRATION_DATE + ", _id FROM " 
						+ CACHE_TABLE + " WHERE "  
						+ IS_EXPIRED + "="  + TRUE; 

		if (is_loaded){
			Cursor result = cache_db.rawQuery(query, null);

			final int num = result.getCount();
			expired = new String[num];
			id_array = new int[num];
			if (result.moveToFirst()){
				for (int i = 0; i < num; i++){
					expired[i] = cacheFileName(	result.getString(0), 
												result.getInt(1), 
												result.getInt(2), 
												result.getInt(3), 
												result.getLong(4));
					id_array[i] = result.getInt(5);
					result.moveToNext();
					//Log.i("CacheManager", "Deleting " + expired[i]);
				}
			}
			result.close();


			Thread file_delete = new Thread(new Runnable(){
				public void run() {
					File file;
					for (int i = 0; i < num; i++){
						file = new File(context.getFilesDir(), expired[i]);
						if (file.exists())
							if(file.delete() && is_loaded)
								cache_db.delete(CACHE_TABLE, "_id=" + Integer.toString(id_array[i]), null);
					}
				}
			}, "File Delete");

			file_delete.start();
		}
	}
	
	public synchronized void close(){
		if (is_loaded){
			cache_helper.close();
			is_loaded = false;
		}
	}
	
	public synchronized void open(){
		if (!is_loaded){
			cache_helper = new CacheDBHelper(context);
			is_loaded = loadCache();
		}
	}
	
	public synchronized boolean loadCache(){
		try{
			cache_db = cache_helper.getWritableDatabase();
		}
		catch (SQLiteException sqle){
			cache_db = cache_helper.getReadableDatabase();
			return false;
		}
		return true;
	}
	
	/**
	 * Helper function to create the file name for a given product file.
	 * @param time - Volume Scan Time
	 * @param date - Volume Scan Date
	 * @return The file name.
	 */
	public static String cacheFileName(String site, int type, int angle, int seq_num, long time_in_millis){
		return 	site + "_" 
				+ String.format("%03d", type) + "_" 
				+ String.format("%02d", angle) + "_" 
				+ String.format("%02d", seq_num) + "_" 
				+ Long.toString(time_in_millis) + ".rad";
	}

	public static String cacheFileName(RadarProduct product) {
		return 	product.site + "_" 
				+ String.format("%03d", product.prod_code) + "_"  
				+ String.format("%02d", DatabaseQuery.getProductAngleFromURL(product.url)) + "_" 
				+ String.format("%02d", product.prod_block.vol_scan_num) + "_" 
				+ Long.toString(product.getExpirationDate()) 
				+ ".rad";
	}
	
	//	/**
	//	 * Returns the time the image was created in milliseconds UTC.
	//	 * @return Time created in milliseconds
	//	 */
	//	public static long getTimeCreatedInMillis(int vol_scan_date, int vol_scan_time){
	//		return (long) (vol_scan_date * 24 * 3600 + vol_scan_time) * 1000;
	//	}

	public boolean isInCache(int prod_code, String site, int prod_angle, int seq_num){
		if (seq_num > 0){
			String query = 	"SELECT " + EXPIRATION_DATE + ", _id FROM " 
			+ CACHE_TABLE + " WHERE " 
			+ PROD_TYPE + "=\"" + Integer.toString(prod_code) + "\" AND " 
			+ SITE_ID + "=\"" + site  + "\" AND "
			+ PROD_ANGLE + "=\"" + Integer.toString(prod_angle) + "\" AND " 
			+ SCAN_NUM + "=\"" + Integer.toString(seq_num) + "\" AND " 
			+ IS_EXPIRED + "=0"; 


			Cursor result = cache_db.rawQuery(query, null);
			return result.getCount() > 0;
		}
		else
			return false;
	}
	
	public DataInputStream loadFromCache(int prod_code, String site, int prod_angle, int seq_num) throws Exception{
		String query = 	"SELECT " + EXPIRATION_DATE + ", _id FROM " 
						+ CACHE_TABLE + " WHERE " 
						+ PROD_TYPE + "=\"" + Integer.toString(prod_code) + "\" AND " 
						+ SITE_ID + "=\"" + site  + "\" AND "
						+ PROD_ANGLE + "=\"" + Integer.toString(prod_angle) + "\" AND " 
						+ SCAN_NUM + "=\"" + Integer.toString(seq_num) + "\" AND " 
						+ IS_EXPIRED + "=0"; 
		long scan_date;

		if (Thread.currentThread().isInterrupted()){
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}

		if (is_loaded){
			Cursor result = cache_db.rawQuery(query, null);
			if (result.moveToFirst()){
				scan_date = result.getLong(0);
				seq_num = result.getInt(1);
			}
			else
				scan_date = 0;
			result.close();
		}
		else
			throw new Exception();

		if (Thread.currentThread().isInterrupted()){
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}

		if (scan_date != 0)
			return loadCacheFile(CacheManager.cacheFileName(site, prod_code, prod_angle, seq_num, scan_date));
		else
			throw new Exception(NO_CACHE_ERROR);
	}
	
	public DataInputStream loadMostRecentFromCache(int prod_code, String site, int prod_angle) throws Exception{
		String query = 	"SELECT " + EXPIRATION_DATE + ", " + SCAN_NUM + ", _id FROM " 
						+ CACHE_TABLE + " WHERE " 
						+ PROD_TYPE + "=\"" + Integer.toString(prod_code) + "\" AND " 
						+ SITE_ID + "=\"" + site  + "\" AND "
						+ PROD_ANGLE + "=\"" + Integer.toString(prod_angle)
						+ "\" ORDER BY " + EXPIRATION_DATE + " DESC";

		long scan_date;
		int seq_num = 0;

		if (Thread.currentThread().isInterrupted()){
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}

		if (is_loaded){
			Cursor result = cache_db.rawQuery(query, null);
			if (result.moveToFirst()){
				scan_date = result.getLong(0);
				seq_num = result.getInt(1);
			}
			else
				scan_date = 0;
			result.close();
		}
		else
			throw new Exception();

		if (Thread.currentThread().isInterrupted()){
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}

		if (scan_date != 0)
			return loadCacheFile(CacheManager.cacheFileName(site, prod_code, prod_angle, seq_num, scan_date));
		else
			throw new Exception(NO_CACHE_ERROR);
	}

	/**
	 * Saves the current collection of arcs into a file that can be loaded 
	 * later, instead of redownloading the file.
	 * @param file_name - File name for the image we are caching.
	 * @return Returns true if the file is successfully created and saved, false otherwise.
	 */
	public boolean cacheProduct(RadarProduct product, DataInputStream product_file){
		try {
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(
							context.openFileOutput(cacheFileName(product), Context.MODE_PRIVATE)
							)
					);
			
			int read = 0;
			byte[] bytes = new byte[1024];
			
			while ( (read = product_file.read(bytes)) != -1)
				output.write(bytes, 0, read);

			output.flush();
			output.close();

			register(product);
			return true;
		} 
		catch (FileNotFoundException e) {
			return false;
		} 
		catch (IOException ie){
			return false;
		}
		
	}
	
	/**
	 * Loads a cached image file from the SD card.
	 * @param file_name - File name of the cached file we want.
	 * @return Returns true if the image was loaded successfully, false otherwise.
	 * @throws FileNotFoundException 
	 * @throws Exception 
	 */
	public DataInputStream loadCacheFile(String file_name) throws FileNotFoundException{
		return new DataInputStream(new BufferedInputStream(context.openFileInput(file_name)));
	}
	
	/**
	 * Formats a Volume Scan Date and Time into a String.
	 * @param scan_date - Volume Scan Date (could also be a Product Gen Date).
	 * @param scan_time - Volume Scan Time (could also be a Product Gen Time).
	 * @return String formatted as "mm/dd/yyyy hh:mm:ss".
	 */
	public static String scanDateToString(int scan_date, int scan_time){
		final int[] date = MJDtoCal(scan_date);
		final int[] time = secToHour(scan_time);
		return String.format("%02d/%02d/%4d %02d:%02d:%02d", date[0], date[1], date[2], time[0], time[1], time[2]);
	}
	
	/**
	 * Formats a Unix time in milliseconds into a String.
	 * @param date_in_millis - Unix time value.
	 * @return String formatted as "mm/dd/yyyy hh:mm:ss".
	 */
	public static String scanDateToString(long date_in_millis){
		final int scan_date = (int)(date_in_millis/1000)/(24 * 3600);
		final int scan_time = (int)(date_in_millis/1000) - scan_date * 24 * 3600;
		final int[] date = MJDtoCal(scan_date);
		final int[] time = secToHour(scan_time);
		return String.format("%02d/%02d/%4d %02d:%02d:%02d", date[0], date[1], date[2], time[0], time[1], time[2]);
	}
}
