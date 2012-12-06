package me.mattsutter.conditionred;

import android.content.Context;

import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import static me.mattsutter.conditionred.MainMapActivity.ANDROID_XML;
import static me.mattsutter.conditionred.MainMapActivity.OPACITY;

public class OpacityPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener{

	private SeekBar seek_bar;
	private TextView value_text;
	private Context context;

	private String suffix;
	private int default_value, max_value, value = 0;
	
	public OpacityPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	    suffix = attrs.getAttributeValue(ANDROID_XML, "text");
	    default_value = attrs.getAttributeIntValue(ANDROID_XML, "defaultValue", 0);
	    max_value = attrs.getAttributeIntValue(ANDROID_XML, "max", 100);
		
	}
	
	@Override 
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6,6,6,6);

		value_text = new TextView(context);
		value_text.setGravity(Gravity.CENTER_HORIZONTAL);
		value_text.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(value_text, params);

		seek_bar = new SeekBar(context);
		seek_bar.setOnSeekBarChangeListener(this);
		layout.addView(	seek_bar, 
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT)
						);

		if (shouldPersist())
			value = getPersistedInt(default_value);

		seek_bar.setMax(max_value);
		seek_bar.setProgress(value);
		return layout;
	}
	
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		seek_bar.setMax(max_value);
		seek_bar.setProgress(value);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		if (positiveResult){
			Editor editor = getEditor();
			editor.putInt(OPACITY, value);
			editor.commit();
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)  
	{
		super.onSetInitialValue(restore, defaultValue);
		if (restore) 
			value = shouldPersist() ? getPersistedInt(default_value) : 0;
		else 
			value = (Integer)defaultValue;
	}
	
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		value = arg1;
		String t = String.valueOf(arg1);
		value_text.setText(suffix == null ? t : t.concat(suffix));
		if (shouldPersist())
			persistInt(arg1);
		callChangeListener(new Integer(arg1));
	}

	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

}
