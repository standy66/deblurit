package tk.standy66.deblurit.tools;

import tk.standy66.deblurit.R;

public class GlobalSettings extends Settings {
	private static String SAVE_PATH = "tk.standy66.deblurit.tools.GlobalSettings_SAVE_PATH";
	private static String MAX_OUTPUT_SIZE = "tk.standy66.deblurit.tools.GlobalSettings_MAX_OUTPUT_SIZE";
	private static String FORMAT = "tk.standy66.deblurit.tools.GlobalSettings_FORMAT";
	private static String MODE = "tk.standy66.deblurit.tools.GlobalSettings_MODE";
	
	public GlobalSettings() {
		super();
		if (App.getApplicationContext() != null) {
			preferencesName = App.getApplicationContext().getResources().getString(R.string.global_settings_preferences_name);
			SAVE_PATH = App.getApplicationContext().getResources().getString(R.string.key_save_path);
			MAX_OUTPUT_SIZE = App.getApplicationContext().getResources().getString(R.string.key_max_output);	
			FORMAT = App.getApplicationContext().getResources().getString(R.string.key_format);
			MODE = App.getApplicationContext().getResources().getString(R.string.mode);
		}
		read();
		getMaxOutputSize();
		getSavePath();
		getFormat();
		getMode();
	}
	
	public String getSavePath() {
		if (map == null)
			return Defaults.getSavePath();
		if (map.get(SAVE_PATH) == null) {
			map.put(SAVE_PATH, Defaults.getSavePath());
			save();
			return getSavePath();
		} else
			return map.get(SAVE_PATH);
	}
	
	public String getFormat() {
		if (map == null)
			return Defaults.getFormat();
		if (map.get(FORMAT) == null) {
			map.put(FORMAT, Defaults.getFormat());
			save();
			return getFormat();
		} else
			return map.get(FORMAT);
	}
	
	public int getMaxOutputSize() {
		if (map == null)
			return Defaults.getMaxOutputSize();
		if (map.get(MAX_OUTPUT_SIZE) == null) {
			map.put(MAX_OUTPUT_SIZE, String.valueOf(Math.min(2048, Defaults.getMaxOutputSize())));
			save();
			return getMaxOutputSize();
		} else {
			int val = Integer.valueOf(map.get(MAX_OUTPUT_SIZE));
			if (val > Defaults.getMaxOutputSize())
				val = Defaults.getMaxOutputSize();
			return val;
		}
	}
	
	public String getMode() {
		if (map == null)
			return Defaults.getMode();
		if (map.get(MODE) == null) {
			map.put(MODE, Defaults.getMode());
			save();
			return getMode();
		} else
			return map.get(MODE);		
	}
	
	public void setSavePath(String path) {
		if (map == null)
			return;
		map.put(SAVE_PATH, path);
		save();
	}
	
	public void setMaxOutputSize(int size) {
		if (map == null)
			return;
		map.put(MAX_OUTPUT_SIZE, String.valueOf(size));
		save();
	}
	
	public void setFormat(String format) {
		if (map == null)
			return;
		map.put(FORMAT, format);
		save();
	}
	
	public void setMode(String mode) {
		if (map == null)
			return;
		map.put(MODE, mode);
		save();
	}
}
