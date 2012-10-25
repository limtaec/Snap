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

import java.util.List;

import kr.teamdeer.snap.RangeSeekBar.OnRangeSeekBarChangeListener;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class GestureLearningActivity extends Activity {
    private SensorManager mSensorManager;    
    private GestureGraphView mGraphView;
	private RangeSeekBar mSeekBar;
	private Button mResetButton;
	private Button mSubmitButton;
	private GestureElement mNewData;
	private GestureEditDialog gedlg;
	private Context context;
    
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
        setContentView(R.layout.new_gesture);
        mGraphView = (GestureGraphView)findViewById(R.id.gestureGraphView);
        mSeekBar = (RangeSeekBar)findViewById(R.id.rangeSeekBar);
        mSeekBar.initFormCode(0.0f, 30.0f);
        mSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar bar,
					float minValue, float maxValue) {
				mGraphView.mStop = true;
				mGraphView.mEditMode = true;
				mGraphView.mRangeL = minValue;
				mGraphView.mRangeR = maxValue;
				mGraphView.Refresh();
			}
        });
        mResetButton = (Button)findViewById(R.id.learnResetBtn);
        mResetButton.setOnClickListener(resetBtnListener);
        mSubmitButton = (Button)findViewById(R.id.learnSubmitButton);
        mSubmitButton.setOnClickListener(submitBtnListener);
        mNewData = new GestureElement();
        context = this;
    }
    
    private OnClickListener resetBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mGraphView.mRecordedDataAcc.clear();
			mGraphView.mRecordedDataOri.clear();
			mGraphView.mEditMode = false;
			mGraphView.mStop = true;
			mSeekBar.setSelectedMaxValue(mSeekBar.getAbsoluteMaxValue());
			mSeekBar.setSelectedMinValue(mSeekBar.getAbsoluteMinValue());
			mNewData = new GestureElement();
			mGraphView.Refresh();
		}  	
    };
    
    private OnDismissListener dissmissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (mNewData.Action.length() < 1) return;
			
			Toast.makeText(context, R.string.gesture_learn_processing, Toast.LENGTH_SHORT).show();
		    if (GestureData.Instance().NeuralNetLearning(getApplicationContext(),mNewData)!=-1) {
		    	Toast.makeText(context, R.string.gesture_learn_succeed, Toast.LENGTH_SHORT).show();
		    } else {
		    	Toast.makeText(context, R.string.gesture_learn_failed, Toast.LENGTH_SHORT).show();
		    }
	        mGraphView.mRecordedDataAcc.clear();
			mGraphView.mRecordedDataOri.clear();
			mGraphView.mEditMode = false;
			mGraphView.mStop = true;
			mSeekBar.setSelectedMaxValue(mSeekBar.getAbsoluteMaxValue());
			mSeekBar.setSelectedMinValue(mSeekBar.getAbsoluteMinValue());
			mNewData = new GestureElement();
			mGraphView.Refresh();
		}
    };
    
    private OnClickListener submitBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {		
			List<Point3> RecordedDataAcc = 
					 mGraphView.mRecordedDataAcc.subList(
					(int)mSeekBar.getSelectedMinValue(),
					Math.min(
							(int)mSeekBar.getSelectedMaxValue(),
							mGraphView.mRecordedDataAcc.size()));
			List<Point2> RecordedDataOri = 
					 mGraphView.mRecordedDataOri.subList(
					(int)mSeekBar.getSelectedMinValue(),
					Math.min(
							(int)mSeekBar.getSelectedMaxValue(),
							mGraphView.mRecordedDataOri.size()));
			while (RecordedDataAcc.size() != 30) {
				RecordedDataAcc.add(new Point3(0,0,0));
			}
			while (RecordedDataOri.size() != 30) {
				RecordedDataOri.add(new Point2(0,0));
			}
			mNewData.PointsAcc.addAll(RecordedDataAcc);
			mNewData.PointsOri.addAll(RecordedDataOri);
			mNewData.Size = Math.min(mNewData.PointsAcc.size(), mNewData.PointsOri.size());
			
			gedlg = new GestureEditDialog(context, dissmissListener, mNewData);
			gedlg.show();
		}  	
    };

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

}
