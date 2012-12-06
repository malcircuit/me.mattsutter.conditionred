package me.mattsutter.conditionred.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_ID;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.PROD_TYPE;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.PROD_ANGLE;

public class CacheDBHelper extends SQLiteOpenHelper {
	public static final String CACHE_DATABASE = "cache.db";
	
	protected static final String CACHE_TABLE = "cache_files";
	protected static final String EXPIRATION_DATE = "exp_date";
	protected static final String IS_EXPIRED = "expired";
	protected static final String SCAN_NUM = "scan_num";
	protected static final int TRUE = 1;
	protected static final int FALSE = 0;
	private static final String CREATE_DB = 	"CREATE TABLE " + CACHE_TABLE 
												+ " (_id INTEGER PRIMARY KEY, " 
												+ SITE_ID + " TEXT, "
												+ PROD_TYPE + " NUMERIC, "
												+ PROD_ANGLE + " NUMERIC, "
												+ SCAN_NUM + " NUMERIC, "
												+ EXPIRATION_DATE + " NUMERIC, "
												+ IS_EXPIRED + " INTEGER )";
	
	private static final int ANIMATION_SUPPORT_ADDED = 2;
	private static final int DATABASE_VERSION = ANIMATION_SUPPORT_ADDED;

	public CacheDBHelper(Context context) {
		super(context, CACHE_DATABASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < ANIMATION_SUPPORT_ADDED){
			db.execSQL("DROP TABLE " + CACHE_TABLE);
			db.execSQL(CREATE_DB);
		}
	}

}
