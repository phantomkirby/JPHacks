package im.ene.androooid.jphacks;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends SensorMonitorActivity implements SensorEventListener, SensorMonitorCallback {
    private static final String TAG = "MainActivity";

    private TextView mTextView;
    private ImageView mImageViewHeart;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mImageViewHeart = (ImageView) stub.findViewById(R.id.imgHeart);

                Animation heartFlashAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.heart_flash);
                mImageViewHeart.startAnimation(heartFlashAnimation);
            }
        });

        setCallback(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onHeartRateChanged(float heartRate, int accuracy) {
        StringBuilder builder = new StringBuilder();
        builder.append("heart rate:");
        builder.append(heartRate);

        mTextView.setText(builder.toString());
    }

    @Override
    public void onStepDetected(int sumOfSteps, int accuracy) {
        StringBuilder builder = new StringBuilder();
        builder.append("walked:");
        builder.append(sumOfSteps);

        mTextView.setText(builder.toString());
    }
}
