package im.ene.androooid.jphacks;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import im.ene.androooid.jphacks.callback.WearSensorCallback;
import im.ene.androooid.jphacks.utils.WearSensorUtil;

//KEEP THIS ACTIVITY JUST IN CASE WE NEED TO LOOK AT THE CODE AGAIN.... I PORTED ALL THIS CODE INTO MAINACTIVTY
public class WearSensorSampleActivity extends Activity implements WearSensorCallback {
    private static final String TAG = "WearSensorSampleActivity";

    private WearSensorUtil mWearSensorUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_sensor_sample);

        mWearSensorUtil = new WearSensorUtil(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWearSensorUtil.setCallback(this);
        mWearSensorUtil.resume();
    }

    @Override
    protected void onPause() {
        mWearSensorUtil.removeCallback();

        super.onPause();
    }

    @Override
    protected void onStop() {
        mWearSensorUtil.stop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mWearSensorUtil.destroy();

        super.onDestroy();
    }

    @Override
    public void onHeartRateChanged(float heartRate) {
        Log.d(TAG, "heart rate:"+heartRate);
    }

    @Override
    public void onStepDetected(int sumOfSteps) {
        Log.d(TAG, "steps:"+sumOfSteps);
    }
}
