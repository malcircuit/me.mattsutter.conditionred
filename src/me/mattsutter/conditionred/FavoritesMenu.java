package me.mattsutter.conditionred;

import static me.mattsutter.conditionred.MainMapActivity.PICK_SITE;
import static me.mattsutter.conditionred.MainMapActivity.SHOW_RADAR_VIEW;
import static me.mattsutter.conditionred.MainMapActivity.CURRENT_SITE_ID;

import me.mattsutter.conditionred.util.DatabaseQuery;
import static me.mattsutter.conditionred.util.DatabaseQueryHelper.SITE_ID;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FavoritesMenu extends ListActivity{
	protected Menu menu;
	private static final int GOTO = 5;
	private static final int ADD_THIS_SITE = 2;
	private static final int ADD_SITE = 3;
	private static final int REMOVE_SITE = 4;
	protected static final String FAVORITES = "favs";
	
	private static final int SITE_MENU_ID = 834;
//	private final int REMOVE_MENU_ID = 345;
	private ArrayAdapter<String> fav_list;
	private int fav_num;
	
	private String selected_site;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radar_site_menu);
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

		fav_list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		fav_num = settings.getInt(FAVORITES, 0);
		
		if (fav_num > 0)
			for (int i=1; i<=fav_num; i++){
				String site = settings.getString(FAVORITES + Integer.toString(i), "KMKX");
				fav_list.add(DatabaseQuery.getSiteCity(site));
			}
		else
			fav_list.add("Add a new site");
		
		this.registerForContextMenu(this.getListView());
		setListAdapter(fav_list);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu _menu){
		super.onCreateOptionsMenu(_menu);
	    	
		menu = _menu;
	    	
		int base = Menu.FIRST;
	    	
		MenuItem item1 = menu.add(base, ADD_THIS_SITE, base, "Add current site");
    	item1.setIcon(R.drawable.ic_menu_goto);

		MenuItem item2 = menu.add(base, ADD_SITE, base+1, "Add new site");
		item2.setIcon(R.drawable.ic_menu_add);
		
//		MenuItem item3 = menu.add(base, REMOVE_SITE, base+2, "Remove Site");
//		item3.setIcon(R.drawable.ic_menu_delete);
		
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case (ADD_THIS_SITE):
			addThisSite();
    		return true;
    	case (ADD_SITE):
        	Intent get_site = new Intent(PICK_SITE);
    		startActivityForResult(get_site, SITE_MENU_ID);
    		return true;
    	//case (REMOVE_SITE):
    		//Intent get_product = new Intent(PRODUCT_MENU);
			//startActivityForResult(get_product, REMOVE_MENU_ID);
    		//return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
	
	@Override
    protected void onActivityResult(int request_code, int result_code, Intent data){
    	if (request_code == SITE_MENU_ID && result_code == RESULT_OK)
    			addSite(data);
    	else
    		super.onActivityResult(request_code, result_code, data);
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		if (fav_num > 0){
			Intent data = new Intent(SHOW_RADAR_VIEW);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
			data.putExtra(CURRENT_SITE_ID, settings.getString(FAVORITES + Integer.toString(position + 1), "KMKX"));
			setResult(RESULT_OK, data);

			finish();
		}
		else{
        	Intent get_site = new Intent(PICK_SITE);
    		startActivityForResult(get_site, SITE_MENU_ID);
		}
	}
	
	protected boolean checkForDuplicates(String site, SharedPreferences settings){
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String city = DatabaseQuery.getSiteCity(site);
		for (int i=0; i < fav_num; i++){
			if (city.equals(fav_list.getItem(i))){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menu_info){
		super.onCreateContextMenu(menu, v, menu_info);

		if (fav_num > 0){
			menu.add(Menu.NONE, GOTO, Menu.FIRST, "Go to site");
			menu.add(Menu.NONE, REMOVE_SITE, Menu.FIRST + 3, "Delete");
		}
		
		menu.add(Menu.NONE, ADD_THIS_SITE, Menu.FIRST + 1, "Add current site");
		menu.add(Menu.NONE, ADD_SITE, Menu.FIRST + 2, "Add new site");
	}
	
	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		AdapterView.AdapterContextMenuInfo menu_info = (AdapterContextMenuInfo) item.getMenuInfo();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

		switch(item.getItemId()){
		case GOTO:
			Intent data = new Intent(SHOW_RADAR_VIEW);
			data.putExtra(CURRENT_SITE_ID, settings.getString(FAVORITES + Integer.toString(menu_info.position + 1), "KMKX"));
			setResult(RESULT_OK, data);
			
			finish();
			break;
		case ADD_THIS_SITE:
			addThisSite();
			break;
		case ADD_SITE:
        	Intent get_site = new Intent(PICK_SITE);
    		startActivityForResult(get_site, SITE_MENU_ID);
    		break;
		case REMOVE_SITE:
			fav_list.remove(fav_list.getItem(menu_info.position));

			SharedPreferences.Editor editor = settings.edit();
			editor.remove(FAVORITES + Integer.toString(menu_info.position + 1));

			String site;
			for (int i = menu_info.position + 1; i < fav_num; i++){
				site = settings.getString(FAVORITES + Integer.toString(i + 1), "KMKX");
				editor.putString(FAVORITES + Integer.toString(i), site);
			}

			fav_num--;
			editor.putInt(FAVORITES, fav_num);
			editor.commit();
			
			if (fav_num ==0)
				fav_list.add("Add a new site");
			break;
		default:
			return false;
		}
		return true;  
	}  
	
	protected void addSite(Intent data){
		selected_site = data.getStringExtra(CURRENT_SITE_ID);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		if (!checkForDuplicates(selected_site, settings)){
			if (fav_num == 0)
				fav_list.clear();
			fav_num++;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(FAVORITES + Integer.toString(fav_num), selected_site);
			editor.putInt(FAVORITES, fav_num);
			editor.commit();
			fav_list.add(DatabaseQuery.getSiteCity(selected_site));
		}
	}
	
	protected void addThisSite(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		selected_site = settings.getString(SITE_ID, "KMKX");
		if (!checkForDuplicates(selected_site, settings)){
			if (fav_num == 0)
				fav_list.clear();
			fav_num++;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(FAVORITES + Integer.toString(fav_num), selected_site);
			editor.putInt(FAVORITES, fav_num);
			editor.commit();
			fav_list.add(DatabaseQuery.getSiteCity(selected_site));
		}
	}
}
