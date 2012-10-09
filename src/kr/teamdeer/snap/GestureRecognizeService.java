package kr.teamdeer.snap;

import android.app.Service;
import android.content.Intent;
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
		//TODO
	}
	
	static {
		System.loadLibrary("Snap");
	}
		
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
