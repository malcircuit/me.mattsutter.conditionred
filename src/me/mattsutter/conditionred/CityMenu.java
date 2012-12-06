package me.mattsutter.conditionred;

import me.mattsutter.conditionred.R;
import me.mattsutter.conditionred.util.DatabaseQuery;
import android.database.Cursor;
import android.app.ListActivity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_CITY;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_STATE;
import static me.mattsutter.conditionred.MainMapActivity.CURRENT_SITE_ID;
import static me.mattsutter.conditionred.MainMapActivity.SHOW_RADAR_VIEW;
import static me.mattsutter.conditionred.MainMapActivity.DEFAULT_SITE;



/** Class that deals with all of the transactions with the radar
 * database.  This is made in an effort to simplify the code elsewhere.
 * 
 * @author Matt Sutter
 */
public class CityMenu extends ListActivity {

	private static final int GOTO = 1;
	private static final int ADD_SITE = 2;
	
	private Cursor city_list;
	private String state;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radar_site_menu);
		
		Intent intent = getIntent();
		
		state = intent.getStringExtra(SITE_STATE);
		
		//Log.i("Main", "Reading city list for " + state + "...");
		city_list = DatabaseQuery.getStateCities(state);

		startManagingCursor(city_list);
		
		//Log.i("onListClick()", "Building menu..." );
		ListAdapter adapter = new SimpleCursorAdapter(
			this,
			android.R.layout.simple_list_item_1,
			city_list,
			new String[] {SITE_CITY,"_id"},
			new int[] {android.R.id.text1}
		);
	
		//Log.i("Main", "Setting menu");
		setListAdapter(adapter);
		this.registerForContextMenu(getListView());
		setResult(RESULT_CANCELED);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent data = new Intent(SHOW_RADAR_VIEW);
		data.putExtra(CURRENT_SITE_ID, getSelectedSite(position));
		setResult(RESULT_OK, data);

		finish();
	}

	@Override
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menu_info){
		super.onCreateContextMenu(menu, v, menu_info);

		menu.add(Menu.NONE, ADD_SITE, Menu.FIRST, "Add site to favorites");
		menu.add(Menu.NONE, GOTO, Menu.FIRST + 1, "Go to site");
	}
	
	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		AdapterView.AdapterContextMenuInfo menu_info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()){
		case GOTO:
			Intent data = new Intent(SHOW_RADAR_VIEW);
			data.putExtra(CURRENT_SITE_ID, getSelectedSite(menu_info.position));
			setResult(RESULT_OK, data);

			finish();
			break;
		case ADD_SITE:
			addSite(getSelectedSite(menu_info.position));
    		break;
		default:
			return false;
		}
		return true;  
	}  
	
	protected void addSite(String site){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		int fav_num = settings.getInt(FavoritesMenu.FAVORITES, 0);
		
		if (!checkForDuplicates(site, fav_num, settings)){
			fav_num++;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(FavoritesMenu.FAVORITES + Integer.toString(fav_num), site);
			editor.putInt(FavoritesMenu.FAVORITES, fav_num);
			editor.commit();
		}

		setResult(RESULT_OK);
	}
	
	private String getSelectedSite(int position){
		city_list.moveToPosition(position);
		String city = city_list.getString(city_list.getColumnIndex(SITE_CITY));

		return DatabaseQuery.getSite(city, state);
	}
	
	private boolean checkForDuplicates(String site, int fav_num, SharedPreferences settings){
		if (fav_num == 0)
			return false;
		
		String buffer;
		for (int i=0; i < fav_num; i++){
			buffer = settings.getString(FavoritesMenu.FAVORITES + Integer.toString(i), DEFAULT_SITE);
			if (site.equals(buffer)){
				return true;
			}
		}
		return false;
	}
}
