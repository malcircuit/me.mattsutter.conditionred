package me.mattsutter.conditionred;

import me.mattsutter.conditionred.R;
import me.mattsutter.conditionred.util.DatabaseQuery;
import android.database.Cursor;
import android.app.ListActivity;
import android.content.*;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static me.mattsutter.conditionred.util.DatabaseQueryHelper.PROD_NAME;
import static me.mattsutter.conditionred.MainMapActivity.CURRENT_PROD_NAME;
import static me.mattsutter.conditionred.MainMapActivity.CURRENT_PROD_TYPE;
import static me.mattsutter.conditionred.MainMapActivity.CURRENT_PROD_URL;
import static me.mattsutter.conditionred.MainMapActivity.SHOW_RADAR_VIEW;


/** Class that deals with all of the transactions with the radar
 * database.  This is made in an effort to simplify the code elsewhere.
 * 
 * @author Matt Sutter
 */
public class ProductsMenu extends ListActivity {

	private Cursor prod_list;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radar_site_menu);
				
		//Log.i("Main", "Reading products ...");
		prod_list = DatabaseQuery.getProductNames();

		startManagingCursor(prod_list);
		
		//Log.i("onListClick()", "Building menu..." );
		ListAdapter adapter = new SimpleCursorAdapter(
			this,
			android.R.layout.simple_list_item_1,
			prod_list,
			new String[] {PROD_NAME, "_id"},
			new int[] {android.R.id.text1}
		);
	
		//Log.i("Main", "Setting menu");
		setListAdapter(adapter);

		setResult(RESULT_CANCELED);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		prod_list.moveToPosition(position);
		
		String product = prod_list.getString( prod_list.getColumnIndex(PROD_NAME) );

		String product_URL = DatabaseQuery.getProductURL(product);
		int product_code = DatabaseQuery.getProductCode(product_URL);
		
		Intent data = new Intent(SHOW_RADAR_VIEW);
		data.putExtra(CURRENT_PROD_NAME, product);
		data.putExtra(CURRENT_PROD_TYPE, product_code);
		data.putExtra(CURRENT_PROD_URL, product_URL);
		
		setResult(RESULT_OK, data);
		
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
