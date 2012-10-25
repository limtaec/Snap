package kr.teamdeer.snap;

import java.util.LinkedList;
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
	public LinkedList<Point3> mRecordedDataAcc;
	public LinkedList<Point2> mRecordedDataOri;
	
	@Override
	public void onCreate() {
		Log.d("GestureRecognizeService", "Service Created");
		super.onCreate();
		GestureData.Instance().load(getApplicationContext());
		if (GestureData.Instance().getGestures().size() == 0)
			stopSelf();
		mHandler = new Handler();
		mSensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
		PowerManager manager =
	            (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		mRecordedDataAcc = new LinkedList<Point3>();
		mRecordedDataOri = new LinkedList<Point2>();
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
		mHandler.postDelayed(this, 500);
		return START_STICKY;
	}
	
	public void run() {
		int result;
		GestureElement tmp = new GestureElement();
		tmp.PointsAcc.addAll(mRecordedDataAcc);
		tmp.PointsOri.addAll(mRecordedDataOri);
		result = GestureData.Instance().NeuralNetRecognize(tmp);
		if (result!=-1) {
			mRecordedDataAcc.clear();
			mRecordedDataOri.clear();
			RunActivity(GestureData.Instance().getGesture(result).Action);
		}
		mHandler.postDelayed(this, 500);
	}
	
	public void onSensorChanged(SensorEvent event) {
		Log.d("GestureRecognizeService", "sensor: " + event.sensor + ", x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2]);
        synchronized (this) {
        	switch (event.sensor.getType()) {
        	case Sensor.TYPE_ACCELEROMETER:
        		Point3 tmp3 = new Point3();
        		tmp3.x = event.values[0];
        		tmp3.y = event.values[1];
        		tmp3.z = event.values[2];
        		mRecordedDataAcc.add(tmp3);
        		if (mRecordedDataAcc.size() > 30) {
            		mRecordedDataAcc.removeFirst();
            	}
        		break;
        	case Sensor.TYPE_ORIENTATION:
        		Point2 tmp2 = new Point2();
        		tmp2.x = event.values[1];
        		tmp2.y = event.values[2];
        		mRecordedDataOri.add(tmp2);
        		if (mRecordedDataOri.size() > 30) {
        			mRecordedDataOri.removeFirst();
            	}
        		break;
        	}       	
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
	
	public void RunActivity(String Action)
	{
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        String Actions[] = Action.split(",");
        String pkgName = Actions[0];
        String name = Actions[1];
        
        if (null != apps) {
         Log.d("ACTIVITY", "apps : " + apps.size());
         ResolveInfo info = null;
         for (ResolveInfo vr : apps) {
        	 if ( pkgName.equals(vr.activityInfo.applicationInfo.packageName) &&
        			 name.equals(vr.activityInfo.name)) {
        		 info = vr;
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
			
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
