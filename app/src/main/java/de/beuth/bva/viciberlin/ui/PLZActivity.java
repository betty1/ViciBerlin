package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.rest.OAuthTwitterCall;
import de.beuth.bva.viciberlin.rest.OAuthYelpCall;
import de.beuth.bva.viciberlin.rest.RestCall;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.DataHandler;
import de.beuth.bva.viciberlin.ui.util.HideShowListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class PLZActivity extends AppCompatActivity implements RestCall.RestCallback, OAuthTwitterCall.OAuthTwitterCallback, OAuthYelpCall.OAuthYelpCallback, DataHandler.DataReceiver {

    @Bind(R.id.age_header) LinearLayout ageHeader;
    @Bind(R.id.age_chart) ColumnChartView ageChart;
    @Bind(R.id.age_linearlayout) LinearLayout ageLinearLayout;
    @Bind(R.id.age_equal_header) LinearLayout ageEqualHeader;
    @Bind(R.id.age_equal_linearlayout) LinearLayout ageEqualLinearLayout;

    @Bind(R.id.gender_header) LinearLayout genderHeader;
    @Bind(R.id.gender_chart) ColumnChartView genderChart;
    @Bind(R.id.gender_linearlayout) LinearLayout genderLinearLayout;

    @Bind(R.id.location_header) LinearLayout locationHeader;
    @Bind(R.id.location_chart) ColumnChartView locationChart;
    @Bind(R.id.location_linearlayout) LinearLayout locationLinearLayout;
    @Bind(R.id.location_equal_header) LinearLayout locationEqualHeader;
    @Bind(R.id.location_equal_linearlayout) LinearLayout locationEqualLinearLayout;

    @Bind(R.id.duration_header) LinearLayout durationHeader;
    @Bind(R.id.duration_chart) ColumnChartView durationChart;
    @Bind(R.id.duration_linearlayout) LinearLayout durationLinearLayout;
    @Bind(R.id.duration_equal_header) LinearLayout durationEqualHeader;
    @Bind(R.id.duration_equal_linearlayout) LinearLayout durationEqualLinearLayout;

    @Bind(R.id.twitter_flowlayout) FlowLayout twitterFlowLayout;
    @Bind(R.id.restaurants_textview) TextView restaurantTextView;
    @Bind(R.id.cafes_textview) TextView cafesTextView;
    @Bind(R.id.nightlife_textview) TextView nightlifeTextView;

    @Bind(R.id.twitter_progressbar) ProgressBar twitterProgressBar;
    @Bind(R.id.yelp_progressbar) ProgressBar yelpProgressBar;

    final String TAG = "PLZActivity";

    final String GOOGLE_LATLONG_CALLID = "googleLatLongCall";
    final String TWITTER_CALLID = "twitterCall";
    final String YELP_RESTAURANT_CALLID = "yelpRestaurantCall";
    final String YELP_NIGHTLIFE_CALLID = "yelpNightlifeCall";
    final String YELP_CAFES_CALLID = "yelpCafesCall";

    Resources res;
    DataHandler dataHandler;
    HideShowListener hideShowListener;

    String plz = "";
    String[] latLong = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        res = getResources();
        dataHandler = new DataHandler(this, this);

        // Set UI Listener
        hideShowListener = new HideShowListener(this);
        ageHeader.setOnClickListener(hideShowListener);
        ageEqualHeader.setOnClickListener(hideShowListener);
        genderHeader.setOnClickListener(hideShowListener);
        locationHeader.setOnClickListener(hideShowListener);
        locationEqualHeader.setOnClickListener(hideShowListener);
        durationHeader.setOnClickListener(hideShowListener);
        durationEqualHeader.setOnClickListener(hideShowListener);

        // Search
        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        Log.d(TAG, "Activity: " + this.getCallingActivity());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            plz = query;
            preOpenViews(Constants.AGE_CHART);
            updatePlz();
        }
        if (Constants.PLZ_INTENT.equals(intent.getAction())) {
            plz = intent.getStringExtra(Constants.PLZ_EXTRA);
            String preopen = intent.getStringExtra(Constants.PREOPEN_EXTRA);
            if(preopen != null){
                preOpenViews(preopen);
            } else {
                preOpenViews(Constants.AGE_CHART);
            }
            updatePlz();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void receiveResponse(String result, String callId) {

        switch(callId){
            // 1. Get lat long values from result
            // 2. Make Twitter call
            case GOOGLE_LATLONG_CALLID:
                latLong = dataHandler.fetchGoogleLatLong(result);
                Log.d(TAG, "Received long lat response" + result);
                twitterCall();
                break;
            // Create Twitter views from result
            case TWITTER_CALLID:
                createTwitterViews(result);
                Log.d(TAG, "Twitter: " + result);
                twitterProgressBar.setVisibility(View.INVISIBLE);
                break;
            // Set Yelp Restaurant Textview
            case YELP_RESTAURANT_CALLID:
                Log.d(TAG, "Yelp: " + result);
                int restaurantCount = dataHandler.fetchYelpTotals(result);
                restaurantTextView.setText(restaurantCount + " ");
                break;
            // Set Yelp Cafes Textview
            case YELP_CAFES_CALLID:
                Log.d(TAG, "Yelp: " + result);
                int cafeCount = dataHandler.fetchYelpTotals(result);
                cafesTextView.setText(cafeCount + " ");
                break;
            // Set Yelp Nightlife Textview
            case YELP_NIGHTLIFE_CALLID:
                Log.d(TAG, "Yelp: " + result);
                int nightlifeCount = dataHandler.fetchYelpTotals(result);
                nightlifeTextView.setText(nightlifeCount + " ");
                yelpProgressBar.setVisibility(View.INVISIBLE);
                break;
        }

    }

    private void preOpenViews(String chartType){
        LinearLayout layout;

        switch(chartType) {
            case Constants.AGE_CHART:
                layout = ageHeader;
                break;
            case Constants.GENDER_CHART:
                layout = genderHeader;
                break;
            case Constants.LOCATION_CHART:
                layout = locationHeader;
                break;
            case Constants.DURATION_CHART:
                layout = durationHeader;
                break;
            default:
                return;
        }
        hideShowListener.onClick(layout);
    }

    private void updatePlz(){

        dataHandler.setPlz(plz);

        // Get name of PLZ region
        String name = dataHandler.fetchPLZName();
        if(name == null){
            Toast.makeText(this, res.getString(R.string.no_berlin_zipcode), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // UI Reaction on new PLZ
        getSupportActionBar().setTitle(plz + " " + name);
        twitterProgressBar.setVisibility(View.VISIBLE);
        yelpProgressBar.setVisibility(View.VISIBLE);

        // fetch Chart Data
        dataHandler.fillCharts();

        // Start Location Call and Yelp Call
        if(plz != null && !plz.equals("")) {
            apiCall("https://maps.googleapis.com/maps/api/geocode/json?address=" + plz + ",berlin", GOOGLE_LATLONG_CALLID);
            yelpCalls();
        }
    }

    private void apiCall(String url, String callId){
        RestCall.startAPICall(url, callId, this);
    }

    private void twitterCall(){
        if(latLong[0] != null && latLong[1] != null){
            String url = "https://api.twitter.com/1.1/search/tweets.json?geocode="
                            + latLong[0] + "," + latLong[1] + ",1km" + "&count=100";
            OAuthTwitterCall.startAPICall(url, TWITTER_CALLID, this);
        }
    }

    private void yelpCalls(){
        OAuthYelpCall.startAPICall("restaurant", plz, YELP_RESTAURANT_CALLID, this);
        OAuthYelpCall.startAPICall("nightlife", plz, YELP_NIGHTLIFE_CALLID, this);
        OAuthYelpCall.startAPICall("cafes", plz, YELP_CAFES_CALLID, this);
    }

    private void createTwitterViews(String result){

        List<String> twitterHashtags = dataHandler.fetchTwitterHashtags(result);
        twitterFlowLayout.removeAllViews();

        if(twitterHashtags.size() == 0){
            twitterFlowLayout.addView(createSingleTwitterView(res.getString(R.string.none), false));
        }

        for(int i=0; i<twitterHashtags.size(); i++){
            if(i>7){
                break;
            }
            String text = "#" + twitterHashtags.get(i);
            twitterFlowLayout.addView(createSingleTwitterView(text, true));
        }
    }

    private LinearLayout createSingleTwitterView(String text, boolean onclick){
        LinearLayout linearLayout = new LinearLayout(this);
        TextView textView = new TextView(this);

        int padding = (int) getResources().getDimension(R.dimen.subitem_padding);
        int margin = (int) getResources().getDimension(R.dimen.subitem_margin);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);

        textView.setTextSize(15);
        textView.setTextColor(Color.WHITE);

        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.subBar, typedValue, true);
        int color = typedValue.data;
        textView.setBackgroundColor(color);

        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);

        if(onclick){
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v;
                    startTwitterIntent((String) tv.getText());
                }
            });
        }
        linearLayout.addView(textView);
        return linearLayout;
    }

    private void startTwitterIntent(String hashtag){

        // Create intent using ACTION_VIEW and a normal Twitter url:
        String url = "https://twitter.com/search?q=%23" + hashtag.substring(1);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }
        startActivity(intent);

    }

    // DataReceiver Interface

    @Override
    public void dataToChart(ChartAttributes attrs, String chartType, int[] colors) {

        float[] values = attrs.values;
        float[] subValues = attrs.subValues;
        String[] axisXLabels = attrs.axisXLabels;

        if(axisXLabels.length != values.length){
            Log.d(TAG, "Wrong number of values/axisXLabels");
            return;
        }

        int numColumns = values.length;
        boolean hasSubColumn = false;

        // Check for subColumnValues
        if(subValues.length == values.length){
            hasSubColumn = true;
        }

        List<SubcolumnValue> tempValues;
        List<AxisValue> xValues = new ArrayList<AxisValue>();;
        List<Column> columns = new ArrayList<Column>();
        int columnColor = 0;
        boolean hasColors = false;

        if(colors != null) {
            if (colors.length == numColumns) {
                hasColors = true;
                Log.d(TAG, "has colors");
            }
        }

        for (int i = 0; i < numColumns; ++i) {

            if(hasColors) {
                columnColor = ContextCompat.getColor(this, colors[i]);
            } else {
                int newColor = ChartUtils.pickColor();
                while(newColor == columnColor){
                    newColor = ChartUtils.pickColor();
                }
                columnColor = newColor;
            }

            tempValues = new ArrayList<SubcolumnValue>();
            tempValues.add(new SubcolumnValue(values[i], columnColor));

            if(hasSubColumn){
                tempValues.add(new SubcolumnValue(subValues[i], ContextCompat.getColor(this, DataHandler.DataReceiver.TRANSGRAY)));
            }

            Column column = new Column(tempValues);
            column.setHasLabels(true);
            columns.add(column);

            xValues.add(new AxisValue(i).setLabel(axisXLabels[i]));
        }

        ColumnChartData data = new ColumnChartData(columns);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName(attrs.axisXName);
        axisY.setName(attrs.axisYName);

        axisX.setValues(xValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        switch (chartType){
            case Constants.AGE_CHART:
                ageChart.setColumnChartData(data);
                break;
            case Constants.GENDER_CHART:
                genderChart.setColumnChartData(data);
                break;
             case Constants.LOCATION_CHART:
                locationChart.setColumnChartData(data);
                break;
             case Constants.DURATION_CHART:
                durationChart.setColumnChartData(data);
                break;
        }


    }

    @Override
    public void dataToViews(List<String> data, String chartType){

        LinearLayout layout;

        switch(chartType) {
            case Constants.AGE_CHART:
                layout = ageEqualLinearLayout;
                break;
            case Constants.LOCATION_CHART:
                layout = locationEqualLinearLayout;
                break;
            case Constants.DURATION_CHART:
                layout = durationEqualLinearLayout;
                break;
            default:
                return;
        }

        layout.removeAllViews();

        for(int i=0; i<data.size(); i++){
            TextView textView = new TextView(this);

            int margin = (int) getResources().getDimension(R.dimen.subitem_margin);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(margin, margin, margin, margin);
            textView.setLayoutParams(params);

            // Set plz and chartType as tags to access them in OnClickListener
            textView.setTag(R.string.plz_tag, data.get(i));
            textView.setTag(R.string.charttype_tag, chartType);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
                    intent.setAction(Constants.PLZ_INTENT);
                    intent.putExtra(Constants.PLZ_EXTRA, (String) v.getTag(R.string.plz_tag));
                    intent.putExtra(Constants.PREOPEN_EXTRA, (String) v.getTag(R.string.charttype_tag));
                    startActivity(intent);
                }
            });

            textView.setText(data.get(i) + " " + dataHandler.fetchPLZName(data.get(i)));
            layout.addView(textView);
        }

    }

}
