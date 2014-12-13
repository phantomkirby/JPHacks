package im.ene.androooid.jphacks;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import im.ene.androooid.jphacks.callback.WearSensorCallback;
import im.ene.androooid.jphacks.utils.WearSensorUtil;
import im.ene.androooid.jphacks.widgets.SquareGifImageByWidth;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnChartValueSelectedListener, WearSensorCallback
{
    //FOR WEAR AND UPDATING THE NUMBER OF STEPS IN BOTTOM LEFT OF ACTIVITY
    private WearSensorUtil mWearSensorUtil;

    //GOOGLE FIT CONSTANT
    private static final int REQUEST_OAUTH = 1;
    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    public static final String TAG = MainActivity.class.getCanonicalName();
    private static final String[] mMonths = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    /**
     * Google Api Client stuffs defined here
     */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequester;
    /**
     * Media router here
     */

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;

    /**
     * Chart
     */

    private BarChart mChart;

    private SquareGifImageByWidth mAvatar;

    private TextView mTextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWearSensorUtil = new WearSensorUtil(this);

        //TODO: call chathead later
        startService(new Intent(this, ChatHeadService.class));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Put application specific code above.

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

        mAvatar = (SquareGifImageByWidth) findViewById(R.id.image_avatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // chart
        mChart = (BarChart) findViewById(R.id.chart_steps_count_week);
        mChart.setOnChartValueSelectedListener(this);

        // enable the drawing of values
        mChart.setDrawYValues(true);

        mChart.setDrawValueAboveBar(true);

        mChart.setDescription("");

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

        XLabels xl = mChart.getXLabels();
        xl.setPosition(XLabels.XLabelPosition.BOTTOM);
        xl.setCenterXLabelText(true);
        xl.setTypeface(tf);

        YLabels yl = mChart.getYLabels();
        yl.setTypeface(tf);
        yl.setLabelCount(8);
        yl.setPosition(YLabels.YLabelPosition.BOTH_SIDED);

        mChart.setValueTypeface(tf);

        setData(7, 50);

        /**
         * test timelytextview
         */

        mTextStep = (TextView) findViewById(R.id.text_step_count);
        mTextStep.setText("0");

        //TODO: CALL THIS METHOD WHEN USER COMES BACK HOME
        //trackUserComingHome();
    }

    private void trackUserComingHome()
    {
        buildFitnessClient();
    }

    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient()
    {
        // Create the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                new RetrieveData().execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    // the asynctask thread
    private class RetrieveData extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... params)
        {
            return retrieveNumberOfStepsAsString();
        }

        @Override
        protected void onPostExecute(String numSteps)
        {
            super.onPostExecute(numSteps);
            Toast.makeText(MainActivity.this, numSteps, Toast.LENGTH_SHORT).show();
        }
    }

    private String retrieveNumberOfStepsAsString()
    {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //original
//        DataReadRequest readRequest = new DataReadRequest.Builder()
//                // The data request can specify multiple data types to return, effectively
//                // combining multiple data queries into one call.
//                // In this example, it's very unlikely that the request is for several hundred
//                // datapoints each consisting of a few steps and a timestamp.  The more likely
//                // scenario is wanting to see how many steps were walked per day, for 7 days.
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
//                        // bucketByTime allows for a time span, whereas bucketBySession would allow
//                        // bucketing by "sessions", which would need to be defined in code.
//                .bucketByTime(1, TimeUnit.DAYS)
//                .setTimeRange(userLeftHomeTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .build();

        //new
        PendingResult<DataReadResult> pendingResult =
                Fitness.HistoryApi.readData(mGoogleApiClient, new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                                //userLeftHomeTime = startTime, current system time = end time
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build());

        //original
//        DataReadResult dataReadResult =
//                Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);
        //new
        DataReadResult dataReadResult = pendingResult.await();


        //TODO: for eneim... put these data into the graph however you like...
        //bucket is basically the days, and the most important number is dp.getValue(field)
        //and field = steps in order to get the value of steps in that day.
        if (dataReadResult.getBuckets().size() > 0)
        {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    Log.i("", "Data returned for Data type: " + dataSet.getDataType().getName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                    for (DataPoint dp : dataSet.getDataPoints()) {
                        Log.i("", "Data pointLOL:");
                        Log.i("", "\tTypeLOL: " + dp.getDataType().getName());
                        Log.i("", "\tStartLOL: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                        Log.i("", "\tEndLOL: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                        for(Field field : dp.getDataType().getFields()) {
                            Log.i("", "\tFieldLOL: " + field.getName() +
                                    " ValueLOL: " + dp.getValue(field));
                            //only this one gets called
                        }
                    }
                }
            }
        }
        else if (dataReadResult.getDataSets().size() > 0) {
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
                        //this one does not get called
                    }
                }
            }
        }

        //CALL DIALOG?
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Add the callback to start device discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        mWearSensorUtil.setCallback(this);
        mWearSensorUtil.resume();
    }

    @Override
    protected void onPause() {
        // Remove the callback to stop device discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        mWearSensorUtil.removeCallback();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
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

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mWearSensorUtil.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }


    public void onValueSelected(Entry entry, int i) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setData(int count, float range)
    {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(mMonths[i % 12]);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        mChart.setData(data);
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback
    {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected");
            // Handle route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            // Just display a message for now; In a real app this would be the
            // hook  to connect to the selected device and launch the receiver
            // app
            Toast.makeText(MainActivity.this,
                    "TODO: Connect", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            mSelectedDevice = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        mWearSensorUtil.destroy();
        super.onDestroy();
    }

    @Override
    public void onHeartRateChanged(float heartRate)
    {
        //do nothing in this implemented method
        //Log.d(TAG, "heart rate:"+heartRate);
    }
    @Override
    public void onStepDetected(int sumOfSteps) {
        Log.d(TAG, "steps:"+sumOfSteps);
        mTextStep.setText(sumOfSteps);
    }
}
