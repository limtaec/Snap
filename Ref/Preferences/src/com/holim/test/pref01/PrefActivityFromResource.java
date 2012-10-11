package com.holim.test.pref01;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PrefActivityFromResource extends PreferenceActivity {
	
	SharedPreferences mainPreference;
	PreferenceCategory category1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 1. \res\xml\preferences.xml�� ���� Preference ���������� �о��
		// 2. �� PreferenceActivity�� ���������� ����/ǥ�� �ϰ�
		// 3. \data\data\��Ű���̸�\shared_prefs\��Ű���̸�_preferences.xml ����
		// 4. �� �� Preference�� ���� ������ ����� ���Ͽ� �ڵ� ����
		addPreferencesFromResource(R.xml.settings);
				
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this); 
		category1 = (PreferenceCategory)findPreference("category1");
		
		// category1�� Ȱ��ȭ ��Ȱ��ȭ ���θ� Preference ���� �� "sub_checkbox"�� Ű ������ ����
		category1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
	}
	
	
	
	// Preference���� Ŭ�� �߻��� ȣ��Ǵ� call back
	// Parameters:
	//  - PreferenceScreen : �̺�Ʈ�� �߻��� Preference�� root
	//  - Preference : �̺�Ʈ�� �߻���Ų Preference �׸�
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		// sub_checkbox�� Ű�� ������ �ִ� Preference�׸��� �̺�Ʈ �߻� �� ���� 
		if(preference.equals((CheckBoxPreference)findPreference("sub_checkbox"))) {
			// Preference ������ ������ "sub_checkbox" Ű�� ����� boolean ���� ����
			// category1 (ListPreference, RingtonePreference ����)�� Ȱ��ȭ/��Ȱ��ȭ
			category1.setEnabled(mainPreference.getBoolean("sub_checkbox", false));
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}	
	
	
	
}