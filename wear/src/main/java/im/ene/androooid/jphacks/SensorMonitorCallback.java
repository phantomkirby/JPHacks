package im.ene.androooid.jphacks;

/**
 * Created by Takahiko on 2014/12/13.
 */
public interface SensorMonitorCallback {
    public void onHeartRateChanged(float heartRate, int accuracy);
    public void onStepDetected(int sumOfSteps, int accuracy);
}
