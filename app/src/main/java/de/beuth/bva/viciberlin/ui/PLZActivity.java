package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.rest.OAuthTwitterCall;
import de.beuth.bva.viciberlin.rest.OAuthYelpCall;
import de.beuth.bva.viciberlin.util.CSVParser;
import de.beuth.bva.viciberlin.rest.RestCall;
import de.beuth.bva.viciberlin.util.HideShowListener;
import de.beuth.bva.viciberlin.util.SortListByFrequency;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class PLZActivity extends AppCompatActivity implements RestCall.RestCallback, OAuthTwitterCall.OAuthTwitterCallback, OAuthYelpCall.OAuthYelpCallback {

    @Bind(R.id.age_header) TextView ageHeader;
    @Bind(R.id.age_chart) ColumnChartView ageChart;
    @Bind(R.id.gender_header) TextView genderHeader;
    @Bind(R.id.gender_chart) ColumnChartView genderChart;
    @Bind(R.id.location_header) TextView locationHeader;
    @Bind(R.id.location_chart) ColumnChartView locationChart;
    @Bind(R.id.duration_header) TextView durationHeader;
    @Bind(R.id.duration_chart) ColumnChartView durationChart;

    @Bind(R.id.twitter_linearlayout) LinearLayout twitterLinearLayout;
    @Bind(R.id.restaurants_textview) TextView restaurantTextView;
    @Bind(R.id.cafes_textview) TextView cafesTextView;
    @Bind(R.id.nightlife_textview) TextView nightlifeTextView;

    final int PURPLE = R.color.graph_purple;
    final int GREEN = R.color.graph_green;
    final int BLUE = R.color.graph_blue;
    final int DARKBLUE = R.color.graph_darkblue;

    final String TAG = "PLZActivity";

    final String GOOGLE_REGION_CALLID = "googleRegionCall";
    final String GOOGLE_LATLONG_CALLID = "googleLatLongCall";
    final String TWITTER_CALLID = "twitterCall";
    final String YELP_RESTAURANT_CALLID = "yelpRestaurantCall";
    final String YELP_NIGHTLIFE_CALLID = "yelpNightlifeCall";
    final String YELP_CAFES_CALLID = "yelpCafesCall";

    String plz = "";
    float area = 1;
    String[] latLong = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plz);
        ButterKnife.bind(this);

        updatePlz();

        // Set UI Listener
        HideShowListener hideShowListener = new HideShowListener(this);
        ageHeader.setOnClickListener(hideShowListener);
        genderHeader.setOnClickListener(hideShowListener);
        locationHeader.setOnClickListener(hideShowListener);
        durationHeader.setOnClickListener(hideShowListener);

        // Search
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            plz = query;
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

    @Override
    public void receiveResponse(String result, String callId) {

        switch(callId){
            case GOOGLE_REGION_CALLID:
                fetchGoogleRegionName(result);
                break;
            case GOOGLE_LATLONG_CALLID:
                fetchGoogleLatLong(result);
                Log.d(TAG, "Received long lat response" + result);
                break;
            case TWITTER_CALLID:
                fetchTwitterHashtags(result);
                Log.d(TAG, "Twitter: " + result);
                break;
            case YELP_RESTAURANT_CALLID:
                Log.d(TAG, "Yelp: " + result);
                fetchYelpTotals(result, restaurantTextView);
                break;
            case YELP_CAFES_CALLID:
                Log.d(TAG, "Yelp: " + result);
                fetchYelpTotals(result, cafesTextView);
                break;
            case YELP_NIGHTLIFE_CALLID:
                Log.d(TAG, "Yelp: " + result);
                fetchYelpTotals(result, nightlifeTextView);
                break;
        }

    }

    private void fetchGoogleRegionName(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray resultArray = jsonResult.getJSONArray("results");

            for(int i=0; i<resultArray.length(); i++){
                JSONArray addressComponents = resultArray.getJSONObject(i).getJSONArray("address_components");

                for(int j=0; j<addressComponents.length(); j++){
                    JSONObject components = addressComponents.getJSONObject(j);
                    JSONArray types = components.getJSONArray("types");

                    if(types.getString(0).equals("sublocality_level_2")){
                        String name = components.getString("short_name");
                        getSupportActionBar().setTitle(plz + " " + name);
                        return;
                    }
                }
            }

        } catch (Exception e){
            Log.d(TAG, "Error parsing Google Region JSON");
        }
    }

    private void fetchGoogleLatLong(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray resultArray = jsonResult.getJSONArray("results");
            JSONObject geometry = resultArray.getJSONObject(0).getJSONObject("geometry");
            Log.d(TAG, "Found geometry: " + geometry);
            JSONObject location = geometry.getJSONObject("location");
            String lat = location.getString("lat");
            String lng = location.getString("lng");
            latLong[0] = lat;
            latLong[1] = lng;

            apiCall("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng, GOOGLE_REGION_CALLID);
            twitterCall();

        } catch (Exception e){
            Log.d(TAG, "Error parsing Google Lat Long JSON");
        }
    }

    private void fetchYelpTotals(String result, TextView textView) {
        if(plz != null && !plz.equals("")){
            try {
                JSONObject jsonResult = new JSONObject(result);
                int total = jsonResult.getInt("total");

                int perkm2 = (int)(total / area);

                textView.setText(perkm2 + " ");

            } catch (Exception e){
                Log.d(TAG, "Error parsing Yelp JSON");
            }
        }
    }

    private void fetchTwitterHashtags(String result){

        List<String> hashtagList = new ArrayList<>();
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray statusesArray = jsonResult.getJSONArray("statuses");

            for(int i=0; i<statusesArray.length(); i++){
                JSONObject status = statusesArray.getJSONObject(i);
                JSONArray hashtags = status.getJSONObject("entities").getJSONArray("hashtags");

                for(int j=0; j<hashtags.length(); j++){
                    JSONObject hashtagObject = hashtags.getJSONObject(j);
                    String hashtag = hashtagObject.getString("text");
                    hashtagList.add(hashtag);

                    Log.d(TAG, "Found hashtag: " + hashtag);
                }
            }
        } catch (Exception e){
            Log.d(TAG, "Error parsing Google Region JSON");
        }

        List<String> sortedHashtags = SortListByFrequency.sortByFreq(hashtagList);



        if(sortedHashtags.size() == 0){
            twitterLinearLayout.addView(createTwitterTextView("keine", false));
        }

        for(int i=0; i<sortedHashtags.size(); i++){
            if(i>5){
                break;
            }
            String text = "#" + sortedHashtags.get(i);
            twitterLinearLayout.addView(createTwitterTextView(text, true));
        }

    }

    private TextView createTwitterTextView(String text, boolean onclick){
        TextView textView = new TextView(this);
        int padding = (int) getResources().getDimension(R.dimen.subitem_padding);
        int margin = (int) getResources().getDimension(R.dimen.subitem_margin);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);
        textView.setTextSize(15);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundResource(R.color.colorPrimaryDark);
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

        return textView;
    }

    private void updatePlz(){
        fillCharts();
        getSupportActionBar().setTitle(plz);
        if(plz != null && !plz.equals("")) {
            apiCall("https://maps.googleapis.com/maps/api/geocode/json?address=" + plz + ",berlin", GOOGLE_LATLONG_CALLID);
            yelpCalls();
        }
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

    private void fillCharts(){
        float[] ageValues = CSVParser.getFloatValuesForPLZ(this, "age.csv", plz);
        String[] ageLabels = new String[]{"bis 12", "12-18", "18-35", "35-65", "ab 65"};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, null, ageLabels, "Lebensjahre", "Prozent");
        dataToChart(ageAttrs, ageChart, new int[]{GREEN, PURPLE, GREEN, PURPLE, GREEN});

        float[] genderValues = CSVParser.getFloatValuesForPLZ(this, "gender.csv", plz);
        String[] genderLabels = new String[]{"Männer", "Frauen"};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, null, genderLabels, "", "Prozent");
        dataToChart(genderAttrs, genderChart, new int[]{DARKBLUE, BLUE});

        float[] locationValues = CSVParser.getFloatValuesForPLZ(this, "wohnlage.csv", plz, 1);
        String[] locationLabels = new String[]{"Einfach", "Mittel", "Gut"};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, null, locationLabels, "", "Prozent");
        dataToChart(locationAttrs, locationChart, null);

        float[] durationValues = CSVParser.getFloatValuesForPLZ(this, "wohndauer.csv", plz);
        String[] durationLabels = new String[]{"unter 5 Jahre", "5-10 Jahre", "über 10 Jahre"};
        ChartAttributes durationAttrs = new ChartAttributes(durationValues, null, durationLabels, "", "Prozent");
        dataToChart(durationAttrs, durationChart, null);

        area = CSVParser.getFloatValuesForPLZ(this, "area.csv", plz)[0];
    }

    private void dataToChart(ChartAttributes attrs, ColumnChartView chart, int[] colors) {

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
                tempValues.add(new SubcolumnValue(subValues[i], columnColor));
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

        chart.setColumnChartData(data);
    }


}
