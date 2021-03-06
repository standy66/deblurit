package tk.standy66.deblurit;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import tk.standy66.deblurit.tools.App;
import tk.standy66.deblurit.tools.Defaults;
import tk.standy66.deblurit.tools.Utils;
import tk.standy66.helper.AppCompatPreferenceActivity;

public class GlobalPreferenceActivity extends AppCompatPreferenceActivity implements OnSharedPreferenceChangeListener
{
    private static int prefs=R.xml.preferences;
    private static Resources resources;
    private static GlobalPreferenceActivity listener;
    
    private static EditTextPreference savePath;
    private static EditTextPreference maxOutput;
    private static ListPreference format;
    private static ListPreference mode;
    private static String[] modes = App.getApplicationContext().getResources().getStringArray(R.array.mode_values);
    private static String[] modesLocalized = App.getApplicationContext().getResources().getStringArray(R.array.modes_localized);


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        App.setApplicationContext(getApplicationContext());
        resources = getResources();
        listener = this;
        super.onCreate(savedInstanceState);
        Log.i("GlobalPreferencesActivity", getPreferenceManager().getSharedPreferencesName());
        try {
            getClass().getMethod("getFragmentManager");
            AddResourceApi11AndGreater();
        } catch (NoSuchMethodException e) { //Api < 11
            AddResourceApiLessThan11();
        }
    }

    @SuppressWarnings("deprecation")
    protected void AddResourceApiLessThan11()
    {
        addPreferencesFromResource(prefs);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        savePath = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_save_path));
        maxOutput = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_max_output));
        format = (ListPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_format));
        mode = (ListPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.mode));
        updateSummary(maxOutput.getKey());
        updateSummary(savePath.getKey());
        updateSummary(format.getKey());
        updateSummary(mode.getKey());
        
    }

    @TargetApi(11)
    protected void AddResourceApi11AndGreater()
    {
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PF()).commit();
    }

    @TargetApi(11)
    public static class PF extends PreferenceFragment
    {       
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(GlobalPreferenceActivity.prefs);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
            savePath = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_save_path));
            maxOutput = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_max_output));
            format = (ListPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_format));
            mode = (ListPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.mode));
            updateSummary(maxOutput.getKey());
            updateSummary(savePath.getKey());
            updateSummary(format.getKey());
            updateSummary(mode.getKey());
            
        }
        
    }
    
    public static void updateSummary(String key, String value) {
        if (key == savePath.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_save_path));
            savePath.setSummary(String.format(resources.getString(R.string.summary_save_path), value));
        } else if (key == maxOutput.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_max_output));
            maxOutput.setSummary(String.format(resources.getString(R.string.summary_max_output), value));
        } else if (key == format.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_format));
            format.setSummary(String.format(resources.getString(R.string.summary_format), value));
        } else {
            int id = -1;
            Log.i("GlobalPreferenceActivity", mode.getEntry().toString());
            Log.i("GlobalPreferenceActivity", String.format("%s %s %s", modes[0], modes[1], modes[2]));
            Log.i("GlobalPreferenceActivity", String.format("%s %s %s", modesLocalized[0], modesLocalized[1], modesLocalized[2]));
            for (int i = 0; i < modes.length; i++)
                if (modes[i].equals(value))
                    id = i;
            if (id == -1)
                throw new RuntimeException("Cant' find selected mode");
            mode.setSummary(String.format(resources.getString(R.string.summary_mode), modesLocalized[id]));

        }
    }
    
    public static void updateSummary(String key) {
        if (key == savePath.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_save_path));
            savePath.setSummary(String.format(resources.getString(R.string.summary_save_path), savePath.getText()));
        } else if (key == maxOutput.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_max_output));
            maxOutput.setSummary(String.format(resources.getString(R.string.summary_max_output), maxOutput.getText()));
        } else if (key == format.getKey()) {
            Log.i("GlobalPreferences", resources.getString(R.string.summary_format));
            format.setSummary(String.format(resources.getString(R.string.summary_format), format.getEntry()));
        } else {
            int id = -1;
            if (mode.getValue() == null) {
                Log.i("GlobalPreferenceActivity", "mode.getValue() == null");
            } else {
                Log.i("GlobalPreferenceActivity", mode.getValue().toString());
            }
            Log.i("GlobalPreferenceActivity", String.format("%s %s %s", modes[0], modes[1], modes[2]));
            Log.i("GlobalPreferenceActivity", String.format("%s %s %s", modesLocalized[0], modesLocalized[1], modesLocalized[2]));

            for (int i = 0; i < modes.length; i++)
                if (modes[i].equals(mode.getValue()))
                    id = i;
            if (id == -1)
                throw new RuntimeException("Cant' find selected mode");
            mode.setSummary(String.format(resources.getString(R.string.summary_mode), modesLocalized[id]));
        }
    }

    boolean handling = false;
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (!(key.equals(maxOutput.getKey()) ||
                key.equals(mode.getKey()) ||
                key.equals(format.getKey()) ||
                key.equals(savePath.getKey())))
            return;
        if (handling)
            return;
        handling = true;
        String val = sharedPreferences.getString(key, "");
        if (key == maxOutput.getKey()) {
            int maxOutputSize = Defaults.getMaxOutputSize();
            int value = 128;
            boolean changed = false;
            try {
                value = Integer.parseInt(maxOutput.getText());
                if (value > maxOutputSize) {
                    changed = true;
                    value = maxOutputSize;
                } else if (value < 128) {
                    changed = true;
                    value = 128;
                }
            } catch (NumberFormatException e) {
                value = maxOutputSize;
                changed = true;
            }
            if (changed) {
                sharedPreferences.edit().putString(key, String.valueOf(value)).commit();
                val = String.valueOf(value);
            }
        }
        updateSummary(key, val);
        handling = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.analyticsLogScreenChange(getApplication(), "Preferences");
    }
}