package me.mattsutter.conditionred;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.content.*;
import android.graphics.PointF;
//import android.util.Log;
import android.view.*;

//import me.mattsutter.conditionred.util.CacheManager;
import me.mattsutter.conditionred.util.DatabaseQuery;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.*;
import static me.mattsutter.conditionred.products.RadarProduct.E_BASE_REFL;
import me.mattsutter.conditionred.R;

public class MapActivity extends Activity 
implements 	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
			ScaleGestureDetector.OnScaleGestureListener{

	public static final String APP_NAME = "Condition Red";
	public static final String ANDROID_XML = "http://schemas.android.com/apk/res/android";
	
	private static final int FAVORITES_SELECTED = 2;
	private static final int RADAR_SITE_SELECTED = 3;
	private static final int PRODUCTS_SELECTED = 4;
	private static final int SETTINGS_SELECTED = 5;
	private static final int SITE_MENU_ID = 576;
	private static final int PRODUCTS_MENU_ID = 667;
	private static final int FAV_MENU_ID = 987;
	private static final int SETTINGS_MENU_ID = 324;

	public static final String CURRENT_SITE_ID = "me.mattsutter.conditionred.CurrentSiteId";
	public static final String CURRENT_PROD_URL = "me.mattsutter.conditionred.CurrentProdUrl";
	public static final String CURRENT_PROD_NAME = "me.mattsutter.conditionred.CurrentProdName";
	public static final String CURRENT_PROD_TYPE = "me.mattsutter.conditionred.CurrentProdType";
	public static final String OPACITY = "me.mattsutter.conditionred.Opacity";
	public static final String LAST_REFRESH = "me.mattsutter.conditionred.LastRefresh";
	public static final String FULL_RES = "me.mattsutter.conditionred.FullResolution";

	public static final String DEFAULT_SITE = "KMKX";
	public static final String DEFAULT_PROD_URL = "DS.p94r0/";
	public static final int DEFAULT_PROD_TYPE = E_BASE_REFL;
	public static final String DEFAULT_PROD_NAME = "Base Reflectivity 1";
	public static final int DEFAULT_ALPHA = 35;
	public static final boolean DEFAULT_FULL_RES = true;
	
	// Activity Intents
	public static final String SHOW_CITY_MENU = "me.mattsutter.conditionred.intent.action.ShowCityMenu";
	public static final String GET_RADAR_SITE = "me.mattsutter.conditionred.intent.action.ChooseRadarSite";
	public static final String SHOW_RADAR_VIEW = "me.mattsutter.conditionred.intent.action.ShowRadarView";
	public static final String PRODUCT_MENU = "me.mattsutter.conditionred.intent.action.ChooseProduct";
	public static final String FAVS_MENU = "me.mattsutter.conditionred.intent.action.Favorites";
	public static final String PICK_SITE = "me.mattsutter.conditionred.intent.action.ChooseFavorites";
	public static final String SHOW_SETTINGS_MENU = "me.mattsutter.conditionred.intent.action.ShowSettings";
	
	private final Handler handler = new Handler();
	
//	private CustomMapView map_view;
//	private GlOverlay overlay;
	private RadarView radar_view;
	private ConnectivityManager conn_man;
	protected Menu main_menu;
	
	private int prod_type;
	private int radar_alpha = 50;
	
	private String prod_url;
	private String prod_name;
	private String site_id;
	
	private boolean should_refresh = false;
	
	private boolean full_res = true;
	private boolean receiver_registered = false;
	
	/**
	 * BroadcastReceiver for network connection updates.  If we are waiting for 
	 * a good network connection so we can refresh the radar image, and we receive 
	 * a confirmation that the network is now up, this schedules a new radar product
	 * download.
	 */
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized(this){
				if (should_refresh){
					boolean no_conn = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
					if (no_conn)
						return;
					NetworkInfo net_info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					if (net_info != null && net_info.isConnectedOrConnecting()){
						//Log.i("Connection Update", "A good connection is now available!");
						should_refresh = false;
					}
				}
			}
		}
	};
	
	/**
	 * Turns on the spinning wheel thingy in the title bar.
	 */
	public final Runnable progressOn = new Runnable(){
		public void run() {
			setProgressBarIndeterminateVisibility(true);
		}
	};

	/**
	 * Turns off the spinning wheel thingy in the title bar.
	 */
	public final Runnable progressOff = new Runnable(){
		public void run() {
			setProgressBarIndeterminateVisibility(false);
		}
	};
	
//	//TODO: documentation
//	private final CancelableRunnable clearCache = new CancelableRunnable(){
//		public void run(){
//			handler.post(new Runnable(){
//				public void run() {
//					//Log.i("Cache", "Checking for expired cached images...");
//					CacheManager.checkForExpired(site_id);
//					CacheManager.removeExpired();
////					Log.i("Cache", "Done.");
//				}
//			});
//		}
//	};
//
//    /**
//     * Makes a Toast that notifies the user that the radar image can't downloaded
//     * at this time.  Most likely because the NWS server is down, or possibly 
//     * because of a bad connection.
//     */
//    private final Runnable downloadFailed = new Runnable(){
//    	public void run(){
//    		Toast.makeText(getApplicationContext(), "Could not download radar image.", Toast.LENGTH_LONG).show();
//    	}
//    };
//
//	//TODO: documentation
//    private final Runnable noConnection = new Runnable(){
//    	public void run(){
//    		Toast.makeText(getApplicationContext(), "No internet connection is active.", Toast.LENGTH_LONG).show();
//    	}
//    };
//
		
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

		openDatabases();
		
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.radarview);
		setProgressBarIndeterminateVisibility(false);
		radar_view = (RadarView)findViewById(R.id.radar_view);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		synchronized (this){
			prod_url = settings.getString(PROD_URL, DEFAULT_PROD_URL);
			prod_type = settings.getInt(PROD_TYPE, DEFAULT_PROD_TYPE);
			site_id = settings.getString(SITE_ID, DEFAULT_SITE);
			prod_name = settings.getString(PROD_NAME, DEFAULT_PROD_NAME);
			radar_alpha = settings.getInt(OPACITY, DEFAULT_ALPHA);
			full_res = settings.getBoolean(FULL_RES, DEFAULT_FULL_RES);
		}

		changeSite();

	    conn_man = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

	    registerReceivers();
		this.setTitle(site_id + " - " + prod_name);
    }
    
    /**
     * Moves the map and downloads the current product when a new radar site
     * is selected.
     */
	protected void changeSite(){
		//Log.i("Main", "Changing sites...");
		int[] location = DatabaseQuery.getLatLong(site_id);
//		center = new GeoPoint(location[0] * 1000, location[1] * 1000);
//		goToPoint(center);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	
    	main_menu = menu;
    	
    	int base = Menu.FIRST;
    	
    	MenuItem item3 = menu.add(base, FAVORITES_SELECTED, base, "Favorites");
    	item3.setIcon(R.drawable.ic_menu_fav);
    	
    	MenuItem item1 = menu.add(base, RADAR_SITE_SELECTED, base+1, "Radar Sites");
    	item1.setIcon(R.drawable.ic_menu_mapmode);
    	
    	MenuItem item4 = menu.add(base, PRODUCTS_SELECTED, base+2, "Products");
    	item4.setIcon(R.drawable.ic_menu_radar);
    	
    	MenuItem item2 = menu.add(base, SETTINGS_SELECTED, base+3, "Settings");
    	item2.setIcon(R.drawable.ic_menu_preferences);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case FAVORITES_SELECTED:
        	Intent get_favs = new Intent(FAVS_MENU);
			startActivityForResult(get_favs, FAV_MENU_ID);
    		return true;
    	case RADAR_SITE_SELECTED:
        	Intent get_site = new Intent(GET_RADAR_SITE);
    		startActivityForResult(get_site, SITE_MENU_ID);
    		return true;
    	case PRODUCTS_SELECTED:
    		Intent get_product = new Intent(PRODUCT_MENU);
			startActivityForResult(get_product, PRODUCTS_MENU_ID);
    		return true;
    	case SETTINGS_SELECTED:  
    		Intent show_settings = new Intent(SHOW_SETTINGS_MENU);
			startActivityForResult(show_settings, SETTINGS_MENU_ID);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data){
    	if (result_code == RESULT_OK){
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    		SharedPreferences.Editor editor = settings.edit();
    		
    		switch (request_code){
    		case SITE_MENU_ID:
    			// Remember what site you are at now.
    			synchronized (this){
    				site_id = data.getStringExtra(CURRENT_SITE_ID);
    			}

    			// Remember what site you currently are at now.
    			editor.putString(SITE_ID, site_id);
    			editor.commit();

    			this.setTitle(site_id + " - " + prod_name);
    			changeSite();
    			break;
    		case PRODUCTS_MENU_ID:
    			synchronized (this){
    				prod_type = data.getIntExtra(CURRENT_PROD_TYPE, E_BASE_REFL);
    				prod_url = data.getStringExtra(CURRENT_PROD_URL);
    				prod_name = data.getStringExtra(CURRENT_PROD_NAME);
    			}
    			// Remember the product that is currently being displayed.
    			editor.putInt(PROD_TYPE, prod_type);
    			editor.putString(PROD_URL, prod_url);
    			editor.putString(PROD_NAME, prod_name);
    			editor.commit();

    			this.setTitle(site_id + " - " + prod_name);
    			break;
    		case FAV_MENU_ID:
    			// Remember what site you are at now.
    			synchronized (this){
    				site_id = data.getStringExtra(CURRENT_SITE_ID);
    			}

    			// Remember what site you currently are at now.
    			editor.putString(SITE_ID, site_id);
    			editor.commit();

    			this.setTitle(site_id + " - " + prod_name);
    			changeSite();
    			break;
    		case SETTINGS_MENU_ID:
    			synchronized (this){
    				radar_alpha = settings.getInt(OPACITY, 50);
    				full_res = settings.getBoolean(FULL_RES, true);
    			}
//    			overlay.changeImageAlpha((short) radar_alpha);
    			break;
    		default:
    			super.onActivityResult(request_code, result_code, data);
    		}
    	}
    }

	//TODO: documentation
    private void registerReceivers() {
//    	if (location_overlay != null)
//    		location_overlay.enableMyLocation();
    	
    	if (!receiver_registered){
    		registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    		receiver_registered = true;
    	}
    	
    }

	//TODO: documentation
    private void unregisterReceivers(){
//    	if (location_overlay != null)
//    		location_overlay.disableMyLocation();
    	if (receiver_registered){
    		unregisterReceiver(mConnReceiver);
    		receiver_registered = false;
    	}
    }
    
    private void openDatabases(){
//    	CacheManager.open(getApplicationContext());
    	DatabaseQuery.open(getApplicationContext());
    }
    
    private void closeDatabases(){
//    	CacheManager.close();
    	DatabaseQuery.close();
    }
    
//    /**
//     * Moves the map to a new latitude and longitude.
//     * @param latitude
//     * @param longitude
//     */
//    private void goToPoint(GeoPoint location){    	
//    	map_view.getController().setCenter(location);
//    	map_view.getController().zoomToSpan((int)((124/60)*1000000),
//    										(int)((124/60)*1000000)	);
//    }
    
    /**
     * Saves the current state of the app.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(SITE_ID, site_id);
        savedInstanceState.putString(PROD_NAME, prod_name);
        savedInstanceState.putString(PROD_URL, prod_url);
        savedInstanceState.putInt(PROD_TYPE, prod_type);
        savedInstanceState.putInt(OPACITY, radar_alpha);
        savedInstanceState.putBoolean(FULL_RES, full_res);
    }
    
    /**
     * Restores the current state of the app.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
    	super.onRestoreInstanceState(savedInstanceState);
    	if (savedInstanceState == null)
    		restoreState();
    	else
    		restoreState(savedInstanceState);
    }
    
    private void restoreState(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    	prod_url = settings.getString(PROD_URL, DEFAULT_PROD_URL);
    	prod_type = settings.getInt(PROD_TYPE, DEFAULT_PROD_TYPE);
    	site_id = settings.getString(SITE_ID, DEFAULT_SITE);
    	prod_name = settings.getString(PROD_NAME, DEFAULT_PROD_NAME);
    	if (!prod_url.equals(DatabaseQuery.getProductURL(prod_name))){
    		prod_url = DatabaseQuery.getProductURL(prod_name);
    		prod_type = DatabaseQuery.getProductTypeAndAngle(prod_url)[0];
    	}
    	radar_alpha = settings.getInt(OPACITY, DEFAULT_ALPHA);
    	full_res = settings.getBoolean(FULL_RES, DEFAULT_FULL_RES);
    }
    
    private void restoreState(Bundle savedInstanceState){
		prod_type = savedInstanceState.getInt(PROD_TYPE, E_BASE_REFL);
		prod_url = savedInstanceState.getString(PROD_URL);
		prod_name = savedInstanceState.getString(PROD_NAME);
    	if (!prod_url.equals(DatabaseQuery.getProductURL(prod_name))){
    		prod_url = DatabaseQuery.getProductURL(prod_name);
    		prod_type = DatabaseQuery.getProductTypeAndAngle(prod_url)[0];
    	}
		site_id = savedInstanceState.getString(SITE_ID);
		radar_alpha = savedInstanceState.getInt(OPACITY);
		full_res = savedInstanceState.getBoolean(FULL_RES);
    	
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
		setProgressBarIndeterminateVisibility(false);
		unregisterReceivers();
		
		radar_view.onDestroy();

    	closeDatabases();
    	finish();
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
		setProgressBarIndeterminateVisibility(false);
		unregisterReceivers();
		radar_view.onPause();
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
	    openDatabases();
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
		setProgressBarIndeterminateVisibility(false);
		unregisterReceivers();
    }
    
    @Override
    protected void onRestart(){
    	super.onRestart();
	    openDatabases();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	registerReceivers();
	    openDatabases();
	    radar_view.onResume(prod_type, site_id, prod_url);
    }

	public boolean onDoubleTap(MotionEvent e) {
		radar_view.zoomMap(e.getX(), e.getY());
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		radar_view.flingMap(velocityX, velocityY);
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		radar_view.panMap(distanceX, distanceY);
		return true;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	public boolean onScale(ScaleGestureDetector detector) {
		radar_view.zoomMap(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
		return true;
	}

	public boolean onScaleBegin(ScaleGestureDetector detector) {
		radar_view.zoomMap(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector detector) {
		radar_view.zoomMap(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
	}
}