package im.ene.androooid.jphacks;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.YLabels;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import im.ene.androooid.jphacks.callback.WearSensorCallback;
import im.ene.androooid.jphacks.model.SimpleGeofence;
import im.ene.androooid.jphacks.model.SimpleGeofenceStore;
import im.ene.androooid.jphacks.service.ChatHeadService;
import im.ene.androooid.jphacks.service.GeofenceTransitionsIntentService;
import im.ene.androooid.jphacks.utils.StringUtils;
import im.ene.androooid.jphacks.utils.TextToSpeechTask;
import im.ene.androooid.jphacks.utils.WearSensorUtil;
import im.ene.androooid.jphacks.widgets.SquareGifImageByWidth;
import jp.ne.docomo.smt.dev.common.http.AuthApiKey;

import static im.ene.androooid.jphacks.utils.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static im.ene.androooid.jphacks.utils.Constants.GEOFENCE_EXPIRATION_TIME;
import static im.ene.androooid.jphacks.utils.Constants.TODAI_BUILDING_ID;
import static im.ene.androooid.jphacks.utils.Constants.TODAI_BUILDING_LATITUDE;
import static im.ene.androooid.jphacks.utils.Constants.TODAI_BUILDING_LONGITUDE;
import static im.ene.androooid.jphacks.utils.Constants.TODAI_BUILDING_RADIUS_METERS;


public class MainActivity extends ActionBarActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnChartValueSelectedListener, ResultCallback<DataReadResult>, WearSensorCallback {
    public static final String TAG = MainActivity.class.getSimpleName();
    //GOOGLE FIT CONSTANT
    private static final int REQUEST_OAUTH = 1;
    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String[] mMonths = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private static final int LOCATION_INTERVAL = 5000;
    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;
    //FOR WEAR AND UPDATING THE NUMBER OF STEPS IN BOTTOM LEFT OF ACTIVITY
    private WearSensorUtil mWearSensorUtil;
    private boolean authInProgress = false;
    /**
     * Google Api Client stuffs defined here
     */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DataReadRequest mDataReadRequest;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;


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

        AuthApiKey.initializeAuth(StringUtils.DOCOMO_API_KEY);

        // Rather than displayng this activity, simply display a toast indicating that the geofence
        // service is being created. This should happen in less than a second.
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }


        // TODO: call chathead later
        mChatHeadIntent = new Intent(this, ChatHeadService.class);
        mWearSensorUtil = new WearSensorUtil(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getResources()
                                .getString(R.string.app_id))).build();
        // Create a MediaRouter callback for discovery events
        mMediaRouterCallback = new MyMediaRouterCallback();


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
        mChart.setDescription("YOUR STEPS COUNT");
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);
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

        /**
         * test timelytextview
         */

        mTextStep = (TextView) findViewById(R.id.text_step_count);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

//        mTextStep = (TimelyView) findViewById(R.id.text_step_count);
//
        mTextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoViewToTV.class);
                startActivity(intent);
            }
        });

        //TODO: CALL THIS METHOD WHEN USER COMES BACK HOME

        // FIXME (eneim): the app will automatically call necessary stuff by Callbacks
//        trackUserComingHome();
        mTextStep.setText("0");

        //TODO: CALL THIS METHOD WHEN USER COMES BACK HOME
        //trackUserComingHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(mChatHeadIntent);

        // Add the callback to start device discovery
//            mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
//                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        mWearSensorUtil.setCallback(this);
        mWearSensorUtil.resume();
    }

    @Override
    protected void onPause() {
        // Remove the callback to stop device discovery
//        mMediaRouter.removeCallback(mMediaRouterCallback);
        mWearSensorUtil.removeCallback();
        startService(mChatHeadIntent);
        super.onPause();

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

        if (id == R.id.pretendToComeBackHome)
        {
            //simulate
            showNotificationDialog(this, 5, 7);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        new TextToSpeechTask("せつぞくできました").execute();
//        new RetrieveData().execute();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL); // Update location every second

        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, StringUtils.TEST_LOCATION);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceList,
                mGeofenceRequestIntent);
//        Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        mDataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                        //userLeftHomeTime = startTime, current system time = end time
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.HistoryApi.readData(mGoogleApiClient, mDataReadRequest).setResultCallback(this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
        }

        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceRequestIntent);
        }
        mWearSensorUtil.stop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!connectionResult.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
                    MainActivity.this, 0).show();
            return;
        } else {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        if (!authInProgress) {
            try {
                Log.i(TAG, "Attempting to resolve failed connection");
                authInProgress = true;
                connectionResult.startResolutionForResult(MainActivity.this,
                        REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,
                        "Exception while starting resolution activity", e);
            }
        }

    }

    @Override
    public void onValueSelected(Entry entry, int i) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setData(DataReadResult dataReadResult) {

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

    //IMPORTANT: DO NOT CHANGE THIS METHOD... IT WORKS SO LEAVE IT XD
    @Override
    public void onResult(DataReadResult dataReadResult) {

        // if any of these occurs, then there is no result
        if (dataReadResult.getDataSets().size() <= 0 && dataReadResult.getBuckets().size() <= 0)
        {
            Log.e("omg onResult not proceeded", "omg onResult not proceeded");
            return;
        }

        ArrayList<String> xVals = new ArrayList<String>();
        SimpleDateFormat dateFormatForDays = new SimpleDateFormat("MM-dd");

        for (int i = 0; i < 7; i++)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() - i*86400000); // 86400000 = 24 hours
            String formattedDate = dateFormatForDays.format(cal.getTime());
            xVals.add(formattedDate);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        //TODO: for eneim... put these data into the graph however you like...
        //bucket is basically the days, and the most important number is dp.getValue(field)
        //and field = steps in order to get the value of steps in that day.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (int i = 0; i < dataReadResult.getBuckets().size(); i++) {
                List<DataSet> dataSets = dataReadResult.getBuckets().get(i).getDataSets();
                for (DataSet dataSet : dataSets) {
                    Log.i("", "Data returned for Data type: " + dataSet.getDataType().getName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                    for (DataPoint dp : dataSet.getDataPoints()) {
                        Log.i("", "Data pointLOL:");
                        Log.i("", "\tTypeLOL: " + dp.getDataType().getName());
                        Log.i("", "\tStartLOL: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                        Log.i("", "\tEndLOL: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                        for (Field field : dp.getDataType().getFields()) {
                            Log.i("", "\tFieldLOL: " + field.getName() +
                                    " ValueLOL: " + dp.getValue(field));
                            yVals1.add(new BarEntry(dp.getValue(field).asInt(), i));
                        }
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

                for (DataPoint dp : dataSet.getDataPoints()) {
                    Log.i(TAG, "Data point:");
                    Log.i(TAG, "\tType: " + dp.getDataType().getName());
                    Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                    Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                    for (Field field : dp.getDataType().getFields()) {
                        Log.i(TAG, "\tField: " + field.getName() +
                                " Value: " + dp.getValue(field));
                    }
                }
            }
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        mChart.setData(data);
    }

    // TODO: set action for each situation
    private void showNotificationDialog(Context context, int todayResult, int averageResult) {
        if (todayResult > averageResult) {
            // prositive notification here
            AlertDialog dialog = new AlertDialog.Builder(context).setMessage("Good Jobs")
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();

            dialog.show();

        } else {
            // negative notification here
            // prositive notification here
            AlertDialog dialog = new AlertDialog.Builder(context).setMessage("Bad Job... need to exercise. Work out using Phone or TV?")
                    .setNegativeButton("Phone", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO: crashes no matter what
                            Intent intent = new Intent("im.ene.androooid.jphacks.VideoViewToTV");
                            getPackageManager().resolveService(intent,0);
                            intent.setAction("com.google.android.youtube.api.service.START");
                            dialog.dismiss();
                            startActivity(intent);
                        }
                    })
                    .setPositiveButton("TV", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this,VideoViewToTV.class);
                            dialog.dismiss();
                            startActivity(intent);
                        }
                    }).create();

            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        mWearSensorUtil.destroy();
        super.onDestroy();
    }

    @Override
    public void onHeartRateChanged(float heartRate) {
        //do nothing in this implemented method
        Log.d(TAG, "heart rate:"+heartRate);
    }

    @Override
    public void onStepDetected(int sumOfSteps) {
        Log.d(TAG, "steps:" + sumOfSteps);
        mTextStep.setText(sumOfSteps + "");
    }

    // Defines the allowable request types (in this example, we only add geofences).
    private enum REQUEST_TYPE {
        ADD
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected");
            // Handle route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            // Just display a message for now; In a real app this would be the
            // hook  to connect to the selected device and launch the receiver
            // app
//            Toast.makeText(MainActivity.this,
//                    "TODO: Connect", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            mSelectedDevice = null;
        }
    }
}
