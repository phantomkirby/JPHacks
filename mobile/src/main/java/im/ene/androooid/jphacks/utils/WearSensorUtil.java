package im.ene.androooid.jphacks.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;


import im.ene.androooid.jphacks.callback.WearSensorCallback;

/**
 * Created by Takahiko on 2014/12/14.
 */
public class WearSensorUtil implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {
    public static final String TAG = "WearSensorUtil";

    public static final String SENSOR_TYPE_HEART_RATE = "heartRate";
    public static final String SENSOR_TYPE_STEPS = "steps";

    private GoogleApiClient mWearApiClient;
    private WearSensorCallback mCallback;

    private Handler mHandler;

    public WearSensorUtil(Context context) {
        Log.d(TAG, "constructor");

        mWearApiClient = new GoogleApiClient.Builder(context)
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        mWearApiClient.connect();

        mHandler = new Handler();
    }

    public void resume() {
        Log.d(TAG, "resume");

        if( mWearApiClient != null && !( mWearApiClient.isConnected() || mWearApiClient.isConnecting() ) ) {
            Log.d(TAG, "connecting to api...");
            mWearApiClient.connect();
        }
    }

    public void stop() {
        Log.d(TAG, "stop");

        if ( mWearApiClient != null ) {
            Wearable.MessageApi.removeListener( mWearApiClient, this );
            if ( mWearApiClient.isConnected() ) {
                Log.d(TAG, "disconnecting to api...");
                mWearApiClient.disconnect();
            }
        }
    }

    public void destroy() {
        Log.d(TAG, "destroy");

        if( mWearApiClient != null ) {
            Log.d(TAG, "unregistering ConnectionCallbacks");

            mWearApiClient.unregisterConnectionCallbacks( this );
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived");

        String message = new String(messageEvent.getData());

        String arr[] = message.split(":");
        String type = arr[0];
        if (SENSOR_TYPE_HEART_RATE.equals(type)) {
            final float heartRate = Float.parseFloat(arr[1]);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onHeartRateChanged(heartRate);
                }
            });
        } else if(SENSOR_TYPE_STEPS.equals(type)) {
            final int steps = Integer.parseInt(arr[1]);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onStepDetected(steps);
                }
            });
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        Wearable.MessageApi.addListener( mWearApiClient, this );
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    public void setCallback(WearSensorCallback callback) {
        mCallback = callback;
    }

    public void removeCallback() {
        mCallback = null;
    }
}
