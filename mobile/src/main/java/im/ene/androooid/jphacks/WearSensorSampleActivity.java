package im.ene.androooid.jphacks;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import im.ene.androooid.jphacks.callback.WearSensorCallback;
import im.ene.androooid.jphacks.utils.WearSensorUtil;


public class WearSensorSampleActivity extends Activity implements WearSensorCallback {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wear_sensor_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHeartRateChanged(float heartRate) {

    }

    @Override
    public void onStepDetected(int sumOfSteps) {

    }
}
