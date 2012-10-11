// Intent�� �̿��� Preference�� �����ϴ� ����ε� 
// ���� �ذ��� �ȵǴ� critical�� �������� �ֽ��ϴ�.
// main preference�� ���������� ǥ���� �Ǵµ�
// dialog�� ������ �ϸ� WindowManager�� BadTokenException�� �߻���ŵ�ϴ�.
// ������ �ذ� ����� �ƽô� ���� �� ���� �ٶ��ϴ�.

package com.holim.test.pref01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PrefActivityFromIntent extends PreferenceActivity {
	
	SharedPreferences mainPreference;
	PreferenceCategory cat1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		// Intent ����� Preference ������
		// Preference�� �����ϴ� Activity�� androidManifest.xml�� <activity> �ؿ�
		// <meta-data> �ױ׸� ���� �ؾ� ��. androidManefist.xml ����
		addPreferencesFromIntent(new Intent(this, PrefActivityFromResource.class));
				
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this); 
		cat1 = (PreferenceCategory)findPreference("category1");	
	}
	
	
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if(preference.equals((CheckBoxPreference)findPreference("sub_checkbox"))) {
			cat1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
		}	
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}