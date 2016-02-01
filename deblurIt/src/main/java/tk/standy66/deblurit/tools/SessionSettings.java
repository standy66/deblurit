package tk.standy66.deblurit.tools;

import tk.standy66.deblurit.R;
import android.app.Application;

public class SessionSettings extends Settings {
	
	private final static String CHOOSED_BITMAP_PREF = "tk.standy66.deblurit.CHOOSED_BITMAP_URI";
	private final static String SCALED_BITMAP_PREF = "tk.standy66.deblurit.SCALED_BITMAP_URI";
	private final static String SCALE_FACTOR_PREF = "tk.standy66.deblurit.SCALE_FACTOR";

	public SessionSettings() {
		super();
		if (App.getApplicationContext() != null)
			preferencesName = App.getApplicationContext().getResources().getString(R.string.session_settings_preferences_name);
		read();
	}
	
	public String getChoosedBitmap() {
		if (map == null)
			return null;
		return map.get(CHOOSED_BITMAP_PREF);
	}
	
	public String getScaledBitmap() {
		if (map == null)
			return null;
		return map.get(SCALED_BITMAP_PREF);
	}
	
	public float getScaleFactor() {
		if (map == null)
			return 1.0f;
		if (map.get(SCALE_FACTOR_PREF) == null)
			return Float.NaN;
		else
			return Float.valueOf(map.get(SCALE_FACTOR_PREF));
	}
	
	public void setChoosedBitmap(String value) {
		if (map == null)
			return;
		map.put(CHOOSED_BITMAP_PREF, value);
		save();
	}
	
	public void setScaledBitmap(String value) {
		if (map == null)
			return;
		map.put(SCALED_BITMAP_PREF, value);
		save();
	}
	
	public void setScaleFactor(float value) {
		if (map == null)
			return;
		map.put(SCALE_FACTOR_PREF, String.valueOf(value));
		save();
	}
	
	
}
