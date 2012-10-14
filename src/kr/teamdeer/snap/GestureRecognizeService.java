package kr.teamdeer.snap;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class GestureRecognizeService extends Service implements SensorEventListener, Runnable {

	private Handler mHandler;
	private WakeLock mWakeLock;
	private SensorManager mSensorMgr;
	
	@Override
	public void onCreate() {
		Log.d("GestureRecognizeService", "Service Created");
		super.onCreate();
		mHandler = new Handler();
		mSensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
		PowerManager manager =
	            (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        this.registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	@Override
	public void onDestroy() {
		Log.d("GestureRecognizeService", "Service Destroyed");
		unregisterReceiver(mReceiver);
        unregisterListener();
        mWakeLock.release();
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("GestureRecognizeService", "Service Started");
		registerListener();
        mWakeLock.acquire();
		mHandler.postDelayed(this, 300);
		return START_STICKY;
	}
	
	public void run() {
		//TODO
		mHandler.postDelayed(this, 300);
	}
	
	public void onSensorChanged(SensorEvent event) {
		Log.d("GestureRecognizeService", "sensor: " + event.sensor + ", x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2]);
        synchronized (this) { /*
            if (mBitmap != null) {
                final Canvas canvas = mCanvas;
                final Paint paint = mPaint;
                float newX = mLastX + mSpeed;
                    
                int j = (event.sensor.getType() == Sensor.TYPE_ORIENTATION) ? 1 : 0;
                for (int i=0 ; i<3 ; i++) {
                	int k = i+j*3;
                    final float v = mYOffset + event.values[i] * ( j==1 ? 1 : mScale );
                    paint.setColor(mColors[k]);
                    canvas.drawLine(mLastX, mLastValues[k], newX, v, paint);
                    mLastValues[k] = v;
                }
                    
                if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
                	mLastX += mSpeed;
                invalidate();
            } */
        }
	}
	
	private void registerListener() {
		mSensorMgr.registerListener(this,
				mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
		mSensorMgr.registerListener(this, 
				mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
	
	private void unregisterListener() {
		mSensorMgr.unregisterListener(this);
    }
	
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ACTIVITY", "onReceive("+intent+")");
            
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            	Runnable runnable = new Runnable() {
                    public void run() {
                        Log.i("ACTIVITY", "Runnable executing.");
                        unregisterListener();
                        registerListener();
                    }
                };

                new Handler().postDelayed(runnable, 500);
                return;
            }
        }
    };
	
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
	
	public native long NeuralNetRecognize(GestureData gData, GestureElement recvData);
	public native long BoxCollisionRecognize(GestureData gData, GestureElement recvData);
	
	static {
		System.loadLibrary("Snap");
	}
		
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
