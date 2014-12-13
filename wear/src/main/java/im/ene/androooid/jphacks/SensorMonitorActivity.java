package im.ene.androooid.jphacks;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Created by Takahiko on 2014/12/13.
 */
public abstract class SensorMonitorActivity extends Activity implements SensorEventListener {
    private static final String TAG = "SensorMonitorActivity";

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mStepsDetectorSensor;

    private int mHeartRateAccuracy;
    private int mStepsDetectorAccuracy;

    private int mSumOfSteps;

    private SensorMonitorCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepsDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(mHeartRateSensor != null)
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_GAME);
        if(mStepsDetectorSensor != null)
            mSensorManager.registerListener(this, mStepsDetectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this, mHeartRateSensor);

        if(mHeartRateSensor != null)
            mSensorManager.unregisterListener(this, mHeartRateSensor);
        if(mStepsDetectorSensor != null)
            mSensorManager.unregisterListener(this, mStepsDetectorSensor);

        super.onPause();
    }

    public void setCallback(SensorMonitorCallback callback) {
        mCallback = callback;
    }

    public void removeCallback() {
        mCallback = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_HEART_RATE:
                mCallback.onHeartRateChanged(event.values[0], mHeartRateAccuracy);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                mCallback.onStepDetected(mSumOfSteps, mStepsDetectorAccuracy);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (sensor.getType()) {
            case Sensor.TYPE_HEART_RATE:
                mHeartRateAccuracy = accuracy;
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                mStepsDetectorAccuracy = accuracy;
                break;
        }
    }
}
