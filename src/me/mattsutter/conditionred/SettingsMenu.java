package me.mattsutter.conditionred;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import static me.mattsutter.conditionred.MapActivity.FULL_RES;

public class SettingsMenu extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	    
	private Context context;
	private Intent intent;
//	private static final String MY_EMAIL = "matt@conditionred.net";
//	private static final String BUG_EMAIL = "bugs@conditionred.net";
	private static final String G_PLUS_URL = "https://plus.google.com/101323554386807064483";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
        	initSummary(getPreferenceScreen().getPreference(i));
        }
        
        //Preference intro = (Preference) this.findPreference("intro");
        Preference contact_me = (Preference) this.findPreference("contact_me");
        Preference about = (Preference) this.findPreference("about");
        
        context = this.getApplicationContext();
        
        contact_me.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference preference) {
		        intent = new Intent(android.content.Intent.ACTION_VIEW);
				intent.setData(Uri.parse(G_PLUS_URL));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				return true;
			}
        
        });
        
        /*intro.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference preference) {
				return true;
			}
        
        });*/
        
        about.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference preference) {
                AboutDialog dialog = new AboutDialog(SettingsMenu.this);
                dialog.show();
				return true;
			}
        
        });
    }
    
    @Override
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		finish();
	}
    @Override 
    protected void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override 
    protected void onPause() { 
        super.onPause();
        // Unregister the listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);     
    } 

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { 
        updatePrefSummary(findPreference(key));
        setResult(RESULT_OK);
    }

    private void initSummary(Preference p){
    	if (p instanceof PreferenceCategory){
    		PreferenceCategory pCat = (PreferenceCategory)p;
    		for(int i=0;i<pCat.getPreferenceCount();i++){
    			initSummary(pCat.getPreference(i));
    		}
    	}
    	else{
    		updatePrefSummary(p);
    	}

    }

    private void updatePrefSummary(Preference p){
    	if (p.hasKey() && p.getKey().equals(FULL_RES)) {
    		CheckBoxPreference check_pref = (CheckBoxPreference) p; 
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    		check_pref.setSummary(settings.getBoolean(FULL_RES, true) ? "Uncheck to disable redrawing after zooming/panning":"Check to enable redrawing after zooming/panning"); 
    	}
    	/*if (p instanceof OpacityPreference) {
    		OpacityPreference opacity_pref = (OpacityPreference) p; 
    		p.setSummary(opacity_pref.getSummary()); 
    	}*/
    }
}
