package me.mattsutter.conditionred;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RemoveMenu extends ListActivity {
	private ArrayAdapter<String> fav_list;
	private int fav_num;
	private final String FAVORITES = "favs";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radar_site_menu);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

		fav_list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		fav_num = settings.getInt(FAVORITES, 0);
		
		for (int i=1; i<=fav_num; i++){
			fav_list.add(settings.getString(FAVORITES + Integer.toString(i), "KMKX"));
		}
		
		setListAdapter(fav_list);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		finish();
	}
	
	@Override
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		//Log.i("City Menu", "Bye bye");
		setResult(RESULT_CANCELED);
		finish();
	}
	
	
}