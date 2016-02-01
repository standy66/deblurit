package tk.standy66.deblurit.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {

	protected Map<String, String> map;
	protected String preferencesName ;
	
	public Settings() {
		map = new HashMap<String, String>();
	}
	
	protected void save() {
		if (preferencesName == null)
			return;
		SharedPreferences preferences = App.getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_MULTI_PROCESS);
				Editor editor = preferences.edit();
    	for (Entry<String, String> entry : map.entrySet())
			editor.putString(entry.getKey(), entry.getValue());			
		editor.commit();
	}
	
	protected void read() {
		if (preferencesName == null)
			return;
		SharedPreferences preferences = App.getApplicationContext().getSharedPreferences(preferencesName, Context.MODE_MULTI_PROCESS);
    	for (Entry<String, ?> entry : preferences.getAll().entrySet())
    		if (entry.getValue() instanceof String)
    			map.put(entry.getKey(), (String)entry.getValue());
	}
}
