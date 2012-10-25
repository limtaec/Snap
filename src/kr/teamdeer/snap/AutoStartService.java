package kr.teamdeer.snap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoStartService extends BroadcastReceiver {

	SharedPreferences mainPreference;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mainPreference = PreferenceManager.getDefaultSharedPreferences(context); 
		
		if (!mainPreference.getBoolean("ServiceStatus", true)) return;
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			ComponentName cn
				= new ComponentName(context.getPackageName(), 
					GestureRecognizeService.class.getName());
			ComponentName svcName
				= context.startService(new Intent().setComponent(cn));
			
			if (svcName == null) 
		        Log.e("BOOTSVC", "Could not start service " + cn.toString());
        }
	}
}
