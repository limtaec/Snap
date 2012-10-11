package com.holim.test.pref01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {	

	public static final int ID_MENU_PREFERENCES_XML = Menu.FIRST+1;
	public static final int ID_MENU_PREFERENCES_INTENT = Menu.FIRST+2;
		
	SharedPreferences defaultSharedPref;
		
	TextView tvGreeting;
	TextView tvOptional;
	TextView tvRingtone;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// PreferenceActivity�� ������� �ʴ� Preference ���� �� �ʿ� (onCreate, onPause, onResume ����)
	////////////////////////////////////////////////////////////////////////////////////////
	public static final String KEY_PREF_STRING_TEST01 = "String test 01";
	public static final String KEY_PREF_STRING_TEST02 = "String test 02";
	SharedPreferences sharedPrefForThis;
	SharedPreferences sharedPrefCustom;
	////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tvGreeting = (TextView)findViewById(R.id.GreetingMsg);
        tvOptional = (TextView)findViewById(R.id.OptionalMsg);
        tvRingtone = (TextView)findViewById(R.id.Ringtone);
        
        // /data/data/��Ű���̸�/shared_prefs/��Ű���̸�_preferences.xml ���ϰ�
        // ����Ǵ� SharedPreferences �������̽� ���. �ش� ������ ���ٸ� �ȵ���̵尡 �ڵ����� ����
        defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this); 
        
        
        ///////////////////////////////////////////////////////////////////////////////
        // PreferenceActivity�� ������� �ʴ� Preference ���� �� �ʿ� (onPause, onResume ����)
        ///////////////////////////////////////////////////////////////////////////////
        sharedPrefForThis = getPreferences(Context.MODE_PRIVATE);
        sharedPrefCustom = getSharedPreferences("MyCustomePref",
        					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        ///////////////////////////////////////////////////////////////////////////////
    }
    
    
    
    @Override
	protected void onPause() {
		super.onPause();		
		
		///////////////////////////////////////////////////////////////////
		// PreferenceActivity�� ������� �ʴ� Preference ���� �� ����
		///////////////////////////////////////////////////////////////////
		SharedPreferences.Editor editor01 = sharedPrefForThis.edit();
		SharedPreferences.Editor editor02 = sharedPrefCustom.edit();
		
		editor01.putString(KEY_PREF_STRING_TEST01, "getPreferences() �޼��带 ����� Activity �̸��� ���� �ڵ� ������ Preference ������ ���Ͽ� ����� Text");
		editor02.putString(KEY_PREF_STRING_TEST02, "getSharedPreferences() �޼��帣 ����� �����ڰ� ���Ƿ� ������ Preference ������ ����(MyCustomePref.xml)�� ����� text");
		
		// **** �߿� ******
		// commit�� �����Ű�� ������ ����� ������ ������� ���� �Ӵ���
		// Preference ������ ������ ���������� ����
		editor01.commit();
		editor02.commit();
		///////////////////////////////////////////////////////////////////
	}
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
		// App�� DefaultSharedPreference ���Ϸ� ���� ȯ�� ������ ���
		boolean isOptionalMsgShown = defaultSharedPref.getBoolean("GreetingMsg", false);
		String strOptionalMsg = defaultSharedPref.getString("AdditionalMsg", "<None>");
		String strColor = defaultSharedPref.getString("TextColor", "FFFFFFFF");
		String strRingtone = defaultSharedPref.getString("Rington", "<None Selected>");
		
		// Activity�� child view���� ȯ�� ������ ����
		tvGreeting.setTextColor((int)Long.parseLong(strColor, 16));
		tvOptional.setTextColor((int)Long.parseLong(strColor, 16));
		tvOptional.setVisibility(isOptionalMsgShown? View.VISIBLE : View.INVISIBLE);
		tvOptional.setText(strOptionalMsg);
		tvRingtone.setText(strRingtone);		
		tvRingtone.setTextColor((int)Long.parseLong(strColor, 16));
		
		//////////////////////////////////////////////////////////////////////////
		// PreferenceActivity�� ������� �ʴ� Preference ���� �� ���� 
		//////////////////////////////////////////////////////////////////////////
		sharedPrefForThis.getString(KEY_PREF_STRING_TEST01, "Not configured yet");
		sharedPrefCustom.getString(KEY_PREF_STRING_TEST02, "Not configured yet");
		//////////////////////////////////////////////////////////////////////////
	}
    
	
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ID_MENU_PREFERENCES_XML, Menu.NONE, "ȯ�漳��(XML ���ҽ�)");
		menu.add(Menu.NONE, ID_MENU_PREFERENCES_INTENT, Menu.NONE, "ȯ�漳��(Intent)");
		return super.onCreateOptionsMenu(menu);
	}
    
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case ID_MENU_PREFERENCES_XML:
    		// Intent�� ����� �ٸ� Activity�� ���� (Intent ���� ����Ʈ���� �ڼ��� �ٷ� ����
    		startActivity(new Intent(this, PrefActivityFromResource.class));
    		return true;
    	case ID_MENU_PREFERENCES_INTENT:
    		startActivity(new Intent(this, PrefActivityFromIntent.class));
    		return true;
    	default:
    		finish();
    		return super.onOptionsItemSelected(item);
    	}	
	}
}