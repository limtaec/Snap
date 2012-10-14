// Original Source from Android ApiDemos - OS/Sensors

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.teamdeer.snap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GestureLearningActivity extends Activity {
    private SensorManager mSensorManager;
    private GraphView mGraphView;

    private class GraphView extends View implements SensorEventListener
    {
        private Bitmap  mBitmap;
        private Paint   mPaint = new Paint();
        private Canvas  mCanvas = new Canvas();
        private float   mLastValues[] = new float[3*2];
        private int     mColors[] = new int[3*2];
        private float   mLastX;
        private float   mScale;
        private float   mYOffset;
        private float   mMaxX;
        private float   mSpeed = 1.0f;
        private float   mWidth;
        private float   mHeight;
        
        public GraphView(Context context) {
            super(context);
            mColors[0] = Color.argb(192, 255, 64, 64);
            mColors[1] = Color.argb(192, 64, 128, 64);
            mColors[2] = Color.argb(192, 64, 64, 255);
            mColors[3] = Color.argb(192, 64, 255, 255);
            mColors[4] = Color.argb(192, 128, 64, 128);
            mColors[5] = Color.argb(192, 255, 255, 64);
            
            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0xFF000000);
            mYOffset = h * 0.5f;
            mScale = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mWidth = w;
            mHeight = h;
            if (mWidth < mHeight) {
                mMaxX = w;
            } else {
                mMaxX = w-50;
            }
            mLastX = mMaxX;
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Paint paint = mPaint;

                    if (mLastX >= mMaxX) {
                        mLastX = 0;
                        final Canvas cavas = mCanvas;
                        final float yoffset = mYOffset;
                        final float maxx = mMaxX;
                        final float oneG = SensorManager.STANDARD_GRAVITY * mScale;
                        paint.setColor(0xFFFFFFFF);
                        cavas.drawColor(0xFF000000);
                        cavas.drawLine(0, yoffset,      maxx, yoffset,      paint);
                        cavas.drawLine(0, yoffset+oneG, maxx, yoffset+oneG, paint);
                        cavas.drawLine(0, yoffset-oneG, maxx, yoffset-oneG, paint);
                    }
                    canvas.drawBitmap(mBitmap, 0, 0, null);
                }
            }
        }

        public void onSensorChanged(SensorEvent event) {
            //Log.i(TAG, "sensor: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
            synchronized (this) {
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
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
    
    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGraphView = new GraphView(this);
        setContentView(mGraphView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, GestureRecognizeService.class));
        mSensorManager.registerListener(mGraphView,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mGraphView, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mGraphView);
        startService(new Intent(this, GestureRecognizeService.class));
        super.onStop();
    }
    
    public native long NeuralNetLearning(GestureData gData, GestureElement newData);
	public native long BoxCollisionLearning(GestureData gData, GestureElement newData);
    
    static {
		System.loadLibrary("Snap");
	}
	
}
