package im.ene.androooid.jphacks;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.YLabels;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import im.ene.androooid.jphacks.utils.StringUtils;
import im.ene.androooid.jphacks.widgets.SquareGifImageByWidth;

import static im.ene.androooid.jphacks.Constants.ANDROID_BUILDING_ID;
import static im.ene.androooid.jphacks.Constants.ANDROID_BUILDING_LATITUDE;
import static im.ene.androooid.jphacks.Constants.ANDROID_BUILDING_LONGITUDE;
import static im.ene.androooid.jphacks.Constants.ANDROID_BUILDING_RADIUS_METERS;
import static im.ene.androooid.jphacks.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static im.ene.androooid.jphacks.Constants.GEOFENCE_EXPIRATION_TIME;
import static im.ene.androooid.jphacks.Constants.TODAI_BUILDING_ID;
import static im.ene.androooid.jphacks.Constants.TODAI_BUILDING_LATITUDE;
import static im.ene.androooid.jphacks.Constants.TODAI_BUILDING_LONGITUDE;
import static im.ene.androooid.jphacks.Constants.TODAI_BUILDING_RADIUS_METERS;
import static im.ene.androooid.jphacks.Constants.YERBA_BUENA_ID;
import static im.ene.androooid.jphacks.Constants.YERBA_BUENA_LATITUDE;
import static im.ene.androooid.jphacks.Constants.YERBA_BUENA_LONGITUDE;
import static im.ene.androooid.jphacks.Constants.YERBA_BUENA_RADIUS_METERS;


public class MainActivity extends ActionBarActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnChartValueSelectedListener {

    public static final String TAG = MainActivity.class.getCanonicalName();
    private static final String[] mMonths = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    private static final int LOCATION_INTERVAL = 5000;
    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;
    /**
     * Google Api Client stuffs defined here
     */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequester;
    ;
    private Location mLastLocation = StringUtils.TEST_LOCATION;
    // These will store hard-coded geofences in this sample app.
//    private SimpleGeofence mAndroidBuildingGeofence;
//    private SimpleGeofence mYerbaBuenaGeofence;
    private SimpleGeofence mTodaiGeofence;

    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;

    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private REQUEST_TYPE mRequestType;
    /**
     * Chart
     */

    private BarChart mChart;
    private SquareGifImageByWidth mAvatar;
    private TextView mTextStep;
    private volatile ObjectAnimator objectAnimator = null;
    private int mCounter = 0;
    private Intent mChatHeadIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Rather than displayng this activity, simply display a toast indicating that the geofence
        // service is being created. This should happen in less than a second.
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        // TODO: call chathead later
        mChatHeadIntent = new Intent(this, ChatHeadService.class);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Instantiate a new geofence storage area.
        mGeofenceStorage = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences.
        mGeofenceList = new ArrayList<Geofence>();

        /**
         * init the geofences
         * TODO: change logic to real locations?
         */
        createGeofences();

        // chart
        mChart = (BarChart) findViewById(R.id.chart_steps_count_week);
        mChart.setOnChartValueSelectedListener(this);

        // enable the drawing of values
        mChart.setDrawYValues(true);

        mChart.setDrawValueAboveBar(true);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(12000);

        // disable 3D
        mChart.set3DEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        // mChart.setDrawXLabels(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawHorizontalGrid(true);
        mChart.setDrawVerticalGrid(false);
        // mChart.setDrawYLabels(false);

        // sets the text size of the values inside the chart
        mChart.setValueTextSize(10f);

        mChart.setDrawBorder(false);
        // mChart.setBorderPositions(new BorderPosition[] {BorderPosition.LEFT,
        // BorderPosition.RIGHT});

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

//        XLabels xl = mChart.getXLabels();
//        xl.setPosition(XLabels.XLabelPosition.BOTTOM);
//        xl.setCenterXLabelText(false);
//        xl.setTypeface(tf);

        YLabels yl = mChart.getYLabels();
        yl.setTypeface(tf);
        yl.setLabelCount(8);
        yl.setPosition(YLabels.YLabelPosition.LEFT);

        mChart.setValueTypeface(tf);

        setData(3, 100);

        /**
         * test timelytextview
         */

        mTextStep = (TextView) findViewById(R.id.text_step_count);
        mTextStep.setText("" + (int) mChart.getYChartMax());

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(mChatHeadIntent);
    }

    @Override
    protected void onPause() {
        // Remove the callback to stop device discovery
        super.onPause();
        startService(mChatHeadIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequester = LocationRequest.create();
        mLocationRequester.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequester.setInterval(LOCATION_INTERVAL); // Update location every second

        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, StringUtils.TEST_LOCATION);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequester, this);

        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceList,
                mGeofenceRequestIntent);
        Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceRequestIntent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
// If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }

    @Override
    public void onValueSelected(Entry entry, int i) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(mMonths[i % 12]);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            int mult = (int) (range + 1);
            int val = 5000 + (int) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        mChart.setData(data);
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = StringUtils.TEST_LOCATION;
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here. A real app might
     * dynamically create geofences based on the user's location.
     */
    public void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
//        mAndroidBuildingGeofence = new SimpleGeofence(
//                ANDROID_BUILDING_ID,                // geofenceId.
//                ANDROID_BUILDING_LATITUDE,
//                ANDROID_BUILDING_LONGITUDE,
//                ANDROID_BUILDING_RADIUS_METERS,
//                GEOFENCE_EXPIRATION_TIME,
//                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
//        );
//        mYerbaBuenaGeofence = new SimpleGeofence(
//                YERBA_BUENA_ID,                // geofenceId.
//                YERBA_BUENA_LATITUDE,
//                YERBA_BUENA_LONGITUDE,
//                YERBA_BUENA_RADIUS_METERS,
//                GEOFENCE_EXPIRATION_TIME,
//                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
//        );

        mTodaiGeofence = new SimpleGeofence(
                TODAI_BUILDING_ID,                // geofenceId.
                TODAI_BUILDING_LATITUDE,
                TODAI_BUILDING_LONGITUDE,
                TODAI_BUILDING_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        // Store these flat versions in SharedPreferences and add them to the geofence list.
//        mGeofenceStorage.setGeofence(ANDROID_BUILDING_ID, mAndroidBuildingGeofence);
//        mGeofenceStorage.setGeofence(YERBA_BUENA_ID, mYerbaBuenaGeofence);
        mGeofenceStorage.setGeofence(TODAI_BUILDING_ID, mTodaiGeofence);
//        mGeofenceList.add(mAndroidBuildingGeofence.toGeofence());
//        mGeofenceList.add(mYerbaBuenaGeofence.toGeofence());
        mGeofenceList.add(mTodaiGeofence.toGeofence());
    }

    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Defines the allowable request types (in this example, we only add geofences).
    private enum REQUEST_TYPE {
        ADD
    }
}
