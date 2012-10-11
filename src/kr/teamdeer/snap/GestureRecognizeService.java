package kr.teamdeer.snap;

import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class GestureRecognizeService extends Service implements SensorEventListener, Runnable {

	private Handler mHandler;
	private SensorManager mSensorMgr;
	
	@Override
	public void onCreate() {
		Log.e("GestureRecognizeService", "Service Created");
		super.onCreate();
		mHandler = new Handler();
		mSensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
	}
	
	@Override
	public void onDestroy() {
		Log.e("GestureRecognizeService", "Service Destroyed");
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("GestureRecognizeService", "Service Started");
		//TODO Sensor Init
		mHandler.postDelayed(this, 300);
		return START_STICKY;
	}
	
	public void run() {
		//TODO
		mHandler.postDelayed(this, 300);
	}
	
	public void onSensorChanged(SensorEvent event) {
		
	}
	
	public void RunActivity(String pkgName, String name)
	{
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        
        if (null != apps) {
         Log.d("ACTIVITY", "apps : " + apps.size());
         ResolveInfo info = null;
         for (int i=0; i < apps.size(); i++) {
        	 if ( (pkgName == apps.get(i).activityInfo.applicationInfo.packageName) &&
        			 (name == apps.get(i).activityInfo.name)) {
        		 info = apps.get(i);
        	 }
         }
         if (info == null) {
        	Log.e("GestureRecognizeService", "Cannot find Activity : " + pkgName + " " + name);
        	return;
         }
         
         String pkg = info.activityInfo.applicationInfo.packageName;
         String cls = info.activityInfo.name;
         ComponentName componentName = new ComponentName(pkg, cls);
         
         Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            
            startActivity(intent);
        }
	}
	
	public native long NeuralNetRecognize();
	public native long BoxCollisionRecognize();
	
	static {
		System.loadLibrary("Snap");
	}
		
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
