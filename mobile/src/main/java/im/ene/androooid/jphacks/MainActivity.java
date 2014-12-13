package im.ene.androooid.jphacks;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

import im.ene.androooid.jphacks.utils.StringUtils;
import im.ene.androooid.jphacks.widgets.SquareGifImageByWidth;


public class MainActivity extends ActionBarActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnChartValueSelectedListener {

    public static final String TAG = MainActivity.class.getCanonicalName();
    private static final String[] mMonths = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    private static final int LOCATION_INTERVAL = 5000;
    /**
     * Google Api Client stuffs defined here
     */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequester;
    private Location mLastLocation = null;

    /**
     * Chart
     */

    private BarChart mChart;

    private SquareGifImageByWidth mAvatar;

    private TextView mTextStep;
    private volatile ObjectAnimator objectAnimator = null;
    private int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO: call chathead later
        startService(new Intent(this, ChatHeadService.class));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


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
    }

    @Override
    protected void onPause() {
        // Remove the callback to stop device discovery
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequester = LocationRequest.create();
        mLocationRequester.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequester.setInterval(LOCATION_INTERVAL); // Update location every second

        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, StringUtils.TEST_LOCATION);

//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequester, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
        mLastLocation = location;
    }
}
