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

import java.util.LinkedList;

import kr.teamdeer.snap.TouchGestureDetector.OnGenericGestureListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureGraphView extends View implements SensorEventListener
    {
        private Bitmap  mBitmap;
        private Paint   mPaint = new Paint();
        private Paint   mTxtPaint = new Paint();
        private Canvas  mCanvas = new Canvas();
        private int     mColors[] = new int[5];
        private float   mScale[] = new float[3];
        private RectF   mDrawing = new RectF();
        private float   mYOffset;
        private float   mYOffsetA;
        private float   mYOffsetO;
        private float   mMaxX;
        private float   mWidth;
        private float   mHeight;
        public  int     mMaxNode;
        public  float   mRangeL;
        public  float   mRangeR;
        public  boolean mStop = true;
        public  boolean mEditMode = false;
        public  LinkedList<Point3> mRecordedDataAcc;
    	public  LinkedList<Point2> mRecordedDataOri;
    	private TouchGestureDetector mGestureDetector;
        
        public GestureGraphView(Context context) {
            super(context);
            onFinishInflate();
        }
		
		public GestureGraphView(Context context, AttributeSet attrs) {
            this(context,attrs,0);
        }
		
		public GestureGraphView(Context context, AttributeSet attrs, 
				int defStyle) {
           super(context,attrs,defStyle);
        }
		
		public void Refresh() {
			invalidate();
		}
		
		@Override
		protected void onFinishInflate() {
			mColors[0] = Color.argb(255, 255, 64, 64);
            mColors[1] = Color.argb(255, 64, 128, 64);
            mColors[2] = Color.argb(255, 64, 64, 255);
            mColors[3] = Color.argb(255, 64, 255, 255);
            mColors[4] = Color.argb(255, 255, 255, 64);
            mRecordedDataAcc = new LinkedList<Point3>();
    		mRecordedDataOri = new LinkedList<Point2>();
    		mMaxNode = 30;
            mRangeL = 0;
            mRangeR = mMaxNode;
            mGestureDetector = new TouchGestureDetector( getContext(), new GenericGestureListener(), null);
    		mTxtPaint.setAntiAlias(true);
    		mTxtPaint.setColor(Color.argb(255, 255, 255, 255));
		}
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0xFF000000);
            mYOffset = h * 0.5f;
            mYOffsetA = h * 0.25f;
            mYOffsetO = h * 0.75f;
            mScale[0] = - (h * 0.25f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mScale[1] = - (h * 0.25f * (1.0f / 360.0f * 2.0f));
            mScale[2] = - (h * 0.25f * (1.0f / 180.0f * 2.0f));
            mWidth = w;
            mHeight = h;
            if (mWidth < mHeight) {
                mMaxX = w;
            } else {
                mMaxX = w-50;
            }
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Paint paint = mPaint;

                    final Canvas cavas = mCanvas;
                    final float oneG = SensorManager.STANDARD_GRAVITY * mScale[0];
                    final float halfpi = 90 * mScale[1];                  
                    paint.setColor(0xFF7F7F7F);
                    cavas.drawColor(0xFF000000);
                    cavas.drawLine(0, mYOffset,      mMaxX, mYOffset,      paint);
                    cavas.drawLine(0, mYOffsetA,      mMaxX, mYOffsetA,      paint);
                    cavas.drawLine(0, mYOffsetA+oneG, mMaxX, mYOffsetA+oneG, paint);
                    cavas.drawLine(0, mYOffsetA-oneG, mMaxX, mYOffsetA-oneG, paint);
                    cavas.drawLine(0, mYOffsetO,      mMaxX, mYOffsetO,      paint);
                    cavas.drawLine(0, mYOffsetO+halfpi, mMaxX, mYOffsetO+halfpi, paint);
                    cavas.drawLine(0, mYOffsetO-halfpi, mMaxX, mYOffsetO-halfpi, paint);
                    
                    float LastX = 0;
                    float Speed = (mMaxX/mMaxNode);
                    float LastValues[] = new float[5];
                    LastValues[0] = LastValues[1] = LastValues[2] = mYOffsetA;
                    if (mRecordedDataAcc.size() > mMaxNode) {
                		LastValues[0] = mYOffsetA + (float)mRecordedDataAcc.getFirst().x * mScale[0];
                		LastValues[1] = mYOffsetA + (float)mRecordedDataAcc.getFirst().y * mScale[0];
                		LastValues[2] = mYOffsetA + (float)mRecordedDataAcc.getFirst().z * mScale[0];
                    	mRecordedDataAcc.removeFirst();
                	}
                    LastValues[3] = LastValues[4] = mYOffsetO;
                    if (mRecordedDataOri.size() > mMaxNode) {
                    	LastValues[3] = mYOffsetO + (float)mRecordedDataOri.getFirst().x * mScale[1];
                    	LastValues[4] = mYOffsetO + (float)mRecordedDataOri.getFirst().y * mScale[2];
            			mRecordedDataOri.removeFirst();
                	}
                    int count = Math.min(mRecordedDataAcc.size(), mRecordedDataOri.size());
                    for (int i=0 ; i<count; i++) {
	                    float vx;                            
	                    float vy;
	                    float vz; 
	                    vx = mYOffsetA + (float)mRecordedDataAcc.get(i).x * mScale[0];
	                    vy = mYOffsetA + (float)mRecordedDataAcc.get(i).y * mScale[0];
	                    vz = mYOffsetA + (float)mRecordedDataAcc.get(i).z * mScale[0];
	                    paint.setColor(mColors[0]);
	                    cavas.drawLine(LastX, LastValues[0], LastX+Speed, vx, paint);
	                    paint.setColor(mColors[1]);
	                    cavas.drawLine(LastX, LastValues[1], LastX+Speed, vy, paint);
	                    paint.setColor(mColors[2]);
	                    cavas.drawLine(LastX, LastValues[2], LastX+Speed, vz, paint);
	                    LastValues[0] = vx;
	                    LastValues[1] = vy;
	                    LastValues[2] = vz;	                    
	                    vx = mYOffsetO + (float)mRecordedDataOri.get(i).x * mScale[1];
	                    vy = mYOffsetO + (float)mRecordedDataOri.get(i).y * mScale[2];
	                    paint.setColor(mColors[3]);
	                    cavas.drawLine(LastX, LastValues[3], LastX+Speed, vx, paint);
	                    paint.setColor(mColors[4]);
	                    cavas.drawLine(LastX, LastValues[4], LastX+Speed, vy, paint);
	                    LastValues[3] = vx;
	                    LastValues[4] = vy;
	                    LastX += Speed;       
                    }                
                    canvas.drawBitmap(mBitmap, 0, 0, null);
                    
                    if (mStop) {
                    	String pausetxt = "Paused"; 
                    	mTxtPaint.setTextSize(30);
                    	canvas.drawText(pausetxt, (mWidth/2)-(mTxtPaint.measureText(pausetxt)/2), (-(mTxtPaint.ascent()+mTxtPaint.descent())*2), mTxtPaint);
                    	
                    	if (mEditMode) {
                    		paint.setColor(0x7F000000);
                    		mDrawing.set(0, 0, mRangeL*(mMaxX/(float)mMaxNode), mHeight);
                    		canvas.drawRect(mDrawing, paint);
                    		mDrawing.set(mRangeR*(mMaxX/(float)mMaxNode), 0, mWidth, mHeight);
                    		canvas.drawRect(mDrawing, paint);
                    		paint.setColor(0x7FFFFFFF);
                    		canvas.drawLine(mRangeL*(mMaxX/(float)mMaxNode), 0, mRangeL*(mMaxX/(float)mMaxNode), mHeight, paint);
                    		canvas.drawLine(mRangeR*(mMaxX/(float)mMaxNode), 0, mRangeR*(mMaxX/(float)mMaxNode), mHeight, paint);
                    	}
                    }

                }
            }
        }

        public void onSensorChanged(SensorEvent event) {
        	Log.d("GestureRecognizeService", "sensor: " + event.sensor + ", x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2]);
            synchronized (this) {
                if (mBitmap != null) {
                	if (mStop) return;
                	
                	switch (event.sensor.getType()) {
                	case Sensor.TYPE_ACCELEROMETER:
                		Point3 tmp3 = new Point3();
                		tmp3.x = event.values[0];
                		tmp3.y = event.values[1];
                		tmp3.z = event.values[2];
                		mRecordedDataAcc.add(tmp3);                		
                		break;
                	case Sensor.TYPE_ORIENTATION:
                		Point2 tmp2 = new Point2();
                		tmp2.x = event.values[1];
                		tmp2.y = event.values[2];
                		mRecordedDataOri.add(tmp2);                		
                		break;
                	} 
                    invalidate();
                }
            }
        }
            
        private final class GenericGestureListener
        extends OnGenericGestureListener {
			
        	@Override
			public boolean onDown(MotionEvent e) { return true; }

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				mStop = !mStop;
				if (mEditMode) mEditMode = false;
				invalidate();
				return true;
			}			
			
        }
                
        @Override
		public boolean onTouchEvent(MotionEvent event) {
			return mGestureDetector.onTouchEvent(event);
		}

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
    }

