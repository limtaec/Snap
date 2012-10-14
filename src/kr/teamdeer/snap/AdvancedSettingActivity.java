package kr.teamdeer.snap;

import android.content.Intent;
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
	
	@Override
	protected void onPause() {
		startService(new Intent(this, GestureRecognizeService.class));
		super.onPause();
	}

	@Override
	protected void onResume() {
		stopService(new Intent(this, GestureRecognizeService.class));
		super.onResume();
	}
	
}
