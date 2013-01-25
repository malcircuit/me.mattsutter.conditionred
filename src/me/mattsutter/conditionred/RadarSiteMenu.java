package me.mattsutter.conditionred;

import me.mattsutter.conditionred.util.DatabaseQuery;
import me.mattsutter.conditionred.R;
import android.app.*;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
//import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_STATE;
import static me.mattsutter.conditionred.MapActivity.SHOW_CITY_MENU;
import static me.mattsutter.conditionred.MapActivity.GET_RADAR_SITE;
import static me.mattsutter.conditionred.MapActivity.PICK_SITE;
import static me.mattsutter.conditionred.MapActivity.FAVS_MENU;
import static me.mattsutter.conditionred.MapActivity.CURRENT_SITE_ID;

public class RadarSiteMenu extends ListActivity{
	private Cursor state_list;
	private String starting_intent;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radar_site_menu);
		
		//Log.i("Main", "Reading state list...");
		state_list = DatabaseQuery.getStates();
		
		startManagingCursor(state_list);

		//Log.i("Main", "Building menu..." );
		ListAdapter adapter = new SimpleCursorAdapter(
			this,
			android.R.layout.simple_list_item_1,
			state_list,
			new String[] {SITE_STATE,"_id"},
			new int[] {android.R.id.text1});
		
		//Log.i("Main", "Setting menu");
		setListAdapter(adapter);
		
		starting_intent = this.getIntent().getAction();
		setResult(RESULT_CANCELED);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		state_list.moveToPosition(position);
		String state = state_list.getString( state_list.getColumnIndex(SITE_STATE) );
		
		Intent city_menu = new Intent(SHOW_CITY_MENU);
		city_menu.putExtra(SITE_STATE, state);
		
		startActivityForResult(city_menu, 879);
	}
	
	@Override
	protected void onActivityResult(int request_code, int result_code, Intent data){
		super.onActivityResult(request_code, result_code, data);
		//Log.i("State Menu", "Good to go.");
		if (result_code == RESULT_OK){
			if (starting_intent.equals(GET_RADAR_SITE)){
				setResult(RESULT_OK, data);
				finish();
			}
			else if (starting_intent.equals(PICK_SITE)){
				Intent send_site = new Intent(FAVS_MENU);
				send_site.putExtra(CURRENT_SITE_ID, data.getStringExtra(CURRENT_SITE_ID));
				setResult(RESULT_OK, send_site);
				finish();
			}
		}
	}
}
