package kr.teamdeer.snap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class AdvancedSettingActivity extends PreferenceActivity {

	SharedPreferences mainPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		
	}
	
}
