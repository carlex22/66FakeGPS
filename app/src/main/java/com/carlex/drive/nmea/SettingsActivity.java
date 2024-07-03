// SettingsActivity.java
package com.carlex.drive.nmea;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.carlex.drive.R;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.nmeafake_settings);
        
        // Register the listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Handle preference change
        if (key.equals("enable_nmeafake")) {
            boolean enableNmeaFake = sharedPreferences.getBoolean(key, true);
            // Update module behavior based on new preference value
        }
    }
}
