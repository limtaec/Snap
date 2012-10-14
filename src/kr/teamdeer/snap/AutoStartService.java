package kr.teamdeer.snap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStartService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
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
