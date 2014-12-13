package im.ene.androooid.jphacks.callback;

/**
 * Created by Takahiko on 2014/12/14.
 */
public interface WearSensorCallback {
    public void onHeartRateChanged(float heartRate);
    public void onStepDetected(int sumOfSteps);
}
