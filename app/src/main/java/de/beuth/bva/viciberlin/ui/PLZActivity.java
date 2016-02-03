package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.geo.GeoProvider;
import de.beuth.bva.viciberlin.geo.GoogleLocationProvider;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.model.ComparableZipcode;
import de.beuth.bva.viciberlin.rest.OAuthTwitterCall;
import de.beuth.bva.viciberlin.rest.OAuthYelpCall;
import de.beuth.bva.viciberlin.rest.RestCallback;
import de.beuth.bva.viciberlin.rest.VolleyRestProvider;
import de.beuth.bva.viciberlin.ui.util.CustomMiniMapView;
import de.beuth.bva.viciberlin.util.CSVParserForZipCodes;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.DataHandler;
import de.beuth.bva.viciberlin.ui.util.HideShowListener;
import de.beuth.bva.viciberlin.util.UserLoginControl;
import io.techery.properratingbar.ProperRatingBar;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class PLZActivity extends AppCompatActivity implements RestCallback, OAuthTwitterCall.OAuthTwitterCallback, OAuthYelpCall.OAuthYelpCallback, DataHandler.DataReceiver, GoogleLocationProvider.LocationListener {

    final String TAG = "PLZActivity";

    final String GOOGLE_LATLONG_CALLID = "googleLatLongCall";
    final String TWITTER_CALLID = "twitterCall";
    final String YELP_RESTAURANT_CALLID = "yelpRestaurantCall";
    final String YELP_NIGHTLIFE_CALLID = "yelpNightlifeCall";
    final String YELP_CAFES_CALLID = "yelpCafesCall";
    final String RATING_POST_CALLID = "ratingPostCall";
    final String RATING_GET_CALLID = "ratingGetCall";

    // MAP
    @Bind(R.id.map_header) LinearLayout mapHeader;
    @Bind(R.id.mini_map) CustomMiniMapView miniMapView;

    @Bind(R.id.zoom_in_button) Button zoomInButton;
    @Bind(R.id.zoom_out_button) Button zoomOutButton;

    // DEMOGRAPHY
    @Bind(R.id.demography_header) LinearLayout demographyHeader;
    @Bind(R.id.demography_info_icon) ImageView demographyInfoIcon;

    @Bind(R.id.age_header) LinearLayout ageHeader;
    @Bind(R.id.age_linearlayout) LinearLayout ageLayout;
    @Bind(R.id.age_chart) ColumnChartView ageChart;
    @Bind(R.id.age_history_chart) ColumnChartView ageHistoryChart;
    @Bind(R.id.age_equal_header) LinearLayout ageEqualHeader;
    @Bind(R.id.age_equal_linearlayout) LinearLayout ageEqualLinearLayout;

    @Bind(R.id.gender_header) LinearLayout genderHeader;
    @Bind(R.id.gender_linearlayout) LinearLayout genderLayout;
    @Bind(R.id.gender_chart) ColumnChartView genderChart;

    @Bind(R.id.location_header) LinearLayout locationHeader;
    @Bind(R.id.location_linearlayout) LinearLayout locationLayout;
    @Bind(R.id.location_chart) ColumnChartView locationChart;
    @Bind(R.id.location_equal_header) LinearLayout locationEqualHeader;
    @Bind(R.id.location_equal_linearlayout) LinearLayout locationEqualLinearLayout;

    @Bind(R.id.duration_header) LinearLayout durationHeader;
    @Bind(R.id.duration_linearlayout) LinearLayout durationLayout;
    @Bind(R.id.duration_chart) ColumnChartView durationChart;
    @Bind(R.id.duration_equal_header) LinearLayout durationEqualHeader;
    @Bind(R.id.duration_equal_linearlayout) LinearLayout durationEqualLinearLayout;

    @Bind(R.id.foreigners_header) LinearLayout foreignersHeader;
    @Bind(R.id.foreigners_linearlayout) LinearLayout foreignersLayout;
    @Bind(R.id.foreigners_chart) ColumnChartView foreignersChart;
    @Bind(R.id.foreigners_history_chart) ColumnChartView foreignersHistoryChart;
    @Bind(R.id.foreigners_equal_header) LinearLayout foreignersEqualHeader;
    @Bind(R.id.foreigners_equal_linearlayout) LinearLayout foreignersEqualLinearLayout;

    @Bind(R.id.compare_header) LinearLayout compareHeader;
    @Bind(R.id.compare_linearlayout) LinearLayout compareLayout;
    @Bind(R.id.most_equal_linearlayout) LinearLayout mostEqualLayout;
    @Bind(R.id.less_equal_linearlayout) LinearLayout lessEqualLayout;

    // RATING
    @Bind(R.id.rate_this_bar) LinearLayout rateThisBar;
    @Bind(R.id.rating_header) LinearLayout ratingHeader;
    @Bind(R.id.culture_ratingbar) ProperRatingBar cultureRatingBar;
    @Bind(R.id.infrastructure_ratingbar) ProperRatingBar infrastructureRatingBar;
    @Bind(R.id.green_ratingbar) ProperRatingBar greenRatingBar;
    @Bind(R.id.safety_ratingbar) ProperRatingBar safetyRatingBar;

    // YELP
    @Bind(R.id.yelp_header) LinearLayout yelpHeader;
    @Bind(R.id.yelp_progressbar) ProgressBar yelpProgressBar;
    @Bind(R.id.restaurants_textview) TextView restaurantTextView;
    @Bind(R.id.cafes_textview) TextView cafesTextView;
    @Bind(R.id.nightlife_textview) TextView nightlifeTextView;

    // TWITTER
    @Bind(R.id.twitter_header) LinearLayout twitterHeader;
    @Bind(R.id.twitter_progressbar) ProgressBar twitterProgressBar;
    @Bind(R.id.twitter_flowlayout) FlowLayout twitterFlowLayout;

    PopupWindow ratingPopup;
    View ratingPopUpLayout;

    PopupWindow explainPopup;
    View explainPopUpLayout;

    Resources res;
    DataHandler dataHandler;
    VolleyRestProvider restProvider;

    IMapController mapController;

    HideShowListener hideShowListener;

    SimpleCursorAdapter searchAdapter;
    SearchView searchView;
    List<String> suggestions;

    // LOCATION
    private GoogleLocationProvider locationProvider;
    private String zipCodeOfUser;
    private boolean isZipCodeOfUser = false;

    private String plz = "";
    private String[] latLong = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        res = getResources();
        dataHandler = new DataHandler(this, this);
        restProvider = new VolleyRestProvider(this, this);

        locationProvider = new GoogleLocationProvider(this, this);
        locationProvider.setupGoogleApiClient();

        setupMap();
        setupUIListener();

        // Search Intent
        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        setupRatingPopup();
        setupExplainPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        // If User Location is unknown, setup Location Updates
        if(zipCodeOfUser == null){
            locationProvider.setupGoogleApiClient();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationProvider.stopLocationUpdates();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if(!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        configureSearchView(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.location_item:
                if(zipCodeOfUser != null){
                    intent = new Intent(getApplicationContext(), PLZActivity.class);
                    intent.setAction(Constants.PLZ_INTENT);
                    intent.putExtra(Constants.PLZ_EXTRA, zipCodeOfUser);
                    intent.putExtra(Constants.ZIPOFUSER_EXTRA, true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Bitte aktivieren Sie Location Services und warten Sie einen Moment, damit Ihr Standort ermittelt werden kann.", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // API Callbacks
    @Override
    public void receiveResponse(String result, String callId) {

        switch(callId){
            case TWITTER_CALLID:
                // Create Twitter views from result
                createTwitterViews(result);
                Log.d(TAG, "Twitter: " + result);
                twitterProgressBar.setVisibility(View.INVISIBLE);
                break;
            case YELP_RESTAURANT_CALLID:
                // Set Yelp Restaurant Textview
                Log.d(TAG, "Yelp: " + result);
                int restaurantCount = dataHandler.fetchYelpTotals(result);
                restaurantTextView.setText(restaurantCount + " ");
                break;
            case YELP_CAFES_CALLID:
                // Set Yelp Cafes Textview
                Log.d(TAG, "Yelp: " + result);
                int cafeCount = dataHandler.fetchYelpTotals(result);
                cafesTextView.setText(cafeCount + " ");
                break;
            case YELP_NIGHTLIFE_CALLID:
                // Set Yelp Nightlife Textview
                Log.d(TAG, "Yelp: " + result);
                int nightlifeCount = dataHandler.fetchYelpTotals(result);
                nightlifeTextView.setText(nightlifeCount + " ");
                yelpProgressBar.setVisibility(View.INVISIBLE);
                break;
            case RATING_POST_CALLID:
                Log.d(TAG, "Rating Post responded: " + result);
                restProvider.makeGETJSONRequest(Constants.RATING_URL + plz + "/", RATING_GET_CALLID);
        }
    }

    @Override
    public void receiveResponse(JSONObject result, String callId) {
        switch (callId) {
            case GOOGLE_LATLONG_CALLID:
                // 1. Get lat long values from result
                // 2. Make Twitter call
                // 3. Center minimap to lat long
                latLong = dataHandler.fetchGoogleLatLong(result);
                Log.d(TAG, "Received long lat response" + result);
                twitterCall();
                setMapCenter();
                break;
            case RATING_GET_CALLID:
                Log.d(TAG, "Rating GET responded: " + result);
                int[] ratingValues = dataHandler.fetchRatingResults(result);
                if(ratingValues.length == 4){
                    cultureRatingBar.setRating(ratingValues[0]);
                    infrastructureRatingBar.setRating(ratingValues[1]);
                    greenRatingBar.setRating(ratingValues[2]);
                    safetyRatingBar.setRating(ratingValues[3]);
                }
                break;
        }
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

            // Look up intent extra, to know which view to open on create
            String preopen = intent.getStringExtra(Constants.PREOPEN_EXTRA);
            if(preopen != null){
                preOpenViews(preopen);
            } else {
                preOpenViews(Constants.AGE_CHART);
            }

            // Is this the zipcode where the user is located? Then show rating header
            isZipCodeOfUser = intent.getBooleanExtra(Constants.ZIPOFUSER_EXTRA, false);

            updatePlz();
        }
    }

    private void setupUIListener(){
        hideShowListener = new HideShowListener(this);

        ViewGroup[] hideShowHeaders = {mapHeader, demographyHeader, ageHeader, ageEqualHeader,
                genderHeader, locationHeader, locationEqualHeader, durationHeader, durationEqualHeader,
                foreignersHeader, foreignersEqualHeader, compareHeader, ratingHeader, yelpHeader, twitterHeader};

        for(ViewGroup view: hideShowHeaders){
            view.setOnClickListener(hideShowListener);
        }

            // Info popups
        demographyInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explainPopup.showAtLocation(ratingPopUpLayout, Gravity.CENTER, 10, 10);
            }
        });
    }

    private void setupExplainPopup() {

        // Create popup with buttons
        explainPopUpLayout = getLayoutInflater().inflate(R.layout.popup_explain, null); // inflating popup layout
        explainPopup = new PopupWindow(explainPopUpLayout, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true); // creation of popup
        Button closeButton = (Button) explainPopUpLayout.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explainPopup.dismiss();
            }
        });

        // Setup Webview
        WebView webView = (WebView) explainPopUpLayout.findViewById(R.id.explain_webview);
        webView.loadUrl("http://vici-berlin.herokuapp.com/explain/demography");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
    }

    private void setupRatingPopup(){

        // Create popup with buttons
        ratingPopUpLayout = getLayoutInflater().inflate(R.layout.popup_rating, null); // inflating popup layout
        ratingPopup = new PopupWindow(ratingPopUpLayout, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true); // creation of popup

        // Set Button ClickListeners
        Button cancelRatingButton = (Button) ratingPopUpLayout.findViewById(R.id.cancel_rating_button);
        Button saveRatingButton = (Button) ratingPopUpLayout.findViewById(R.id.save_rating_button);
        cancelRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingPopup.dismiss();
            }
        });
        saveRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restProvider.makePOSTRequest(Constants.RATING_URL, RATING_POST_CALLID, getRatingParams());
                ratingPopup.dismiss();
                rateThisBar.setVisibility(View.GONE);
            }
        });

        // Show Rate Header and add onClickListener to open popup
        rateThisBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UserLoginControl.isLoggedIn(v.getContext())) {
                    ratingPopup.showAtLocation(ratingPopUpLayout, Gravity.CENTER, 10, 10);
                } else {
                    // Send to login screen if not logged in
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        if(isZipCodeOfUser){
            rateThisBar.setVisibility(View.VISIBLE);
        }
    }

    private HashMap<String, String> getRatingParams(){
        // Get user id
        String userId = UserLoginControl.getUserId(this);
        if(userId == null){
            return null;
        }

        // Set basic params
        HashMap<String, String> params = new HashMap<>();
        params.put("zipcode", String.valueOf(plz));
        params.put("user_id", String.valueOf(userId));

        // Fetch rating values
        ProperRatingBar cultureRating = (ProperRatingBar) ratingPopUpLayout.findViewById(R.id.culture_ratingbar);
        ProperRatingBar infraRating = (ProperRatingBar) ratingPopUpLayout.findViewById(R.id.infrastructure_ratingbar);
        ProperRatingBar greenRating = (ProperRatingBar) ratingPopUpLayout.findViewById(R.id.green_ratingbar);
        ProperRatingBar safetyRating = (ProperRatingBar) ratingPopUpLayout.findViewById(R.id.safety_ratingbar);

        int culture = cultureRating.getRating();
        int infrastructure = infraRating.getRating();
        int green = greenRating.getRating();
        int safety = safetyRating.getRating();
        // (For now) 'Total' is average out of all values
        int total = (int)Math.round((culture + infrastructure + green + safety)/4.0);

        // Set values to params
        params.put("culture", String.valueOf(culture));
        params.put("infrastructure", String.valueOf(infrastructure));
        params.put("green", String.valueOf(green));
        params.put("safety", String.valueOf(safety));
        params.put("total", String.valueOf(total));

        return params;
    }

    private void setupMap(){
        miniMapView.setTileSource(TileSourceFactory.MAPNIK);

        mapController = miniMapView.getController();
        mapController.setZoom(12);
        miniMapView.setMinZoomLevel(11);
        miniMapView.setMaxZoomLevel(19);

        // Set on default map center
        setMapCenter();

        miniMapView.invalidate();

        View.OnClickListener zoomButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.zoom_in_button){
                    mapController.zoomIn();
                } else if(v.getId() == R.id.zoom_out_button) {
                    mapController.zoomOut();
                }
                setMapCenter();
            }
        };

        zoomInButton.setOnClickListener(zoomButtonListener);
        zoomOutButton.setOnClickListener(zoomButtonListener);
    }

    private void setMapCenter(){
        GeoPoint startPoint;
        try {
            startPoint = new GeoPoint(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));
        } catch(Exception e) {
            startPoint = new GeoPoint(13.3833, 52.5167);
        }
        mapController.setCenter(startPoint);
        Marker startMarker = new Marker(miniMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        miniMapView.getOverlays().add(startMarker);
        miniMapView.invalidate();
    }

    private void configureSearchView(Menu menu){
        // Set up adapter
        searchAdapter = new SimpleCursorAdapter(this,
                R.layout.suggestion_textview,
                null,
                new String[] {"plz"},
                new int[] {android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Configure Suggestions
        searchView.setSuggestionsAdapter(searchAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                String selected = suggestions.get(position);
                String plz = selected.split(" ")[0];
                Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
                intent.setAction(Constants.PLZ_INTENT);
                intent.putExtra(Constants.PLZ_EXTRA, plz);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                populateSearchAdapter(s);
                return false;
            }
        });

        // Dirty work around to set background of search suggestions
        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setDropDownBackgroundResource(R.color.graph_transdarkblue);
    }

    private void populateSearchAdapter(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{ BaseColumns._ID, "plz" });
        suggestions = CSVParserForZipCodes.getZipCodeSuggestionForQuery(this, query);
        for (int i=0; i<suggestions.size(); i++) {
            cursor.addRow(new Object[] {i, suggestions.get(i)});
        }
        searchAdapter.changeCursor(cursor);
    }

    private void preOpenViews(String chartType){
        // Standard pre open views
        hideShowListener.onClick(mapHeader);
        hideShowListener.onClick(demographyHeader);
        hideShowListener.onClick(ratingHeader);
        hideShowListener.onClick(yelpHeader);
        hideShowListener.onClick(twitterHeader);

        // custom preopen views
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
            case Constants.FOREIGNERS_CHART:
                layout = foreignersHeader;
                break;
            default:
                return;
        }
        hideShowListener.onClick(layout);
    }

    private void updatePlz(){

        dataHandler.setPlz(plz);

        // Get name of ZIPCODE_TITLE region
        String name = dataHandler.fetchZipCodeName();
        if(name == null){
            Toast.makeText(this, res.getString(R.string.no_berlin_zipcode), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // UI Reaction on new ZIPCODE_TITLE
        getSupportActionBar().setTitle(plz + " " + name);
        twitterProgressBar.setVisibility(View.VISIBLE);
        yelpProgressBar.setVisibility(View.VISIBLE);

        // fetch Chart Data
        dataHandler.fillCharts();

        // Start Location Call and Yelp Call
        if(plz != null && !plz.equals("")) {
            restProvider.makeGETJSONRequest("https://maps.googleapis.com/maps/api/geocode/json?address=" + plz + ",berlin", GOOGLE_LATLONG_CALLID);
            restProvider.makeGETJSONRequest(Constants.RATING_URL + plz + "/", RATING_GET_CALLID);
            yelpCalls();
        }
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

    private void createEqualRegionViews(List<ComparableZipcode> data, String chartType){
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
            case Constants.FOREIGNERS_CHART:
                layout = foreignersEqualLinearLayout;
                break;
            case Constants.MOST_EQUAL_CHART:
                layout = mostEqualLayout;
                for(ComparableZipcode zipcode: data){
                    Log.d(TAG, zipcode.toString());
                }
                break;
            case Constants.LESS_EQUAL_CHART:
                layout = lessEqualLayout;
                for(ComparableZipcode zipcode: data){
                    Log.d(TAG, zipcode.toString());
                }
                break;
            default:
                return;
        }

        layout.removeAllViews();

        for(int i=0; i<data.size(); i++){
            TextView textView = new TextView(this);
            String zipcode = data.get(i).getName();

            int margin = (int) getResources().getDimension(R.dimen.subitem_margin);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(margin, margin, margin, margin);
            textView.setLayoutParams(params);

            // Set plz and chartType as tags to access them in OnClickListener
            textView.setTag(R.string.plz_tag, zipcode);
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

            textView.setText(zipcode + " " + dataHandler.fetchZipCodeName(zipcode));
            layout.addView(textView);
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
                ageChart.setZoomEnabled(false);
                break;
            case Constants.AGE_HISTORY_CHART:
                ageHistoryChart.setColumnChartData(data);
                ageHistoryChart.setZoomEnabled(false);
                break;
            case Constants.GENDER_CHART:
                genderChart.setColumnChartData(data);
                genderChart.setZoomEnabled(false);
                break;
             case Constants.LOCATION_CHART:
                locationChart.setColumnChartData(data);
                locationChart.setZoomEnabled(false);
                break;
             case Constants.DURATION_CHART:
                durationChart.setColumnChartData(data);
                durationChart.setZoomEnabled(false);
                break;
            case Constants.FOREIGNERS_CHART:
                foreignersChart.setColumnChartData(data);
                foreignersChart.setZoomEnabled(false);
                break;
            case Constants.FOREIGNERS_HISTORY_CHART:
                foreignersHistoryChart.setColumnChartData(data);
                foreignersHistoryChart.setZoomEnabled(false);
                break;
        }


    }

    @Override
    public void onLocationUpdate(Location loc) {
        zipCodeOfUser = GeoProvider.plzFromLatLng(this, loc.getLatitude(), loc.getLongitude());
        Log.d(TAG, "New Location: " + zipCodeOfUser);

        if(zipCodeOfUser != null){

            locationProvider.stopLocationUpdates();

            if(zipCodeOfUser.equals(plz)){

                isZipCodeOfUser = true;
                rateThisBar.setVisibility(View.VISIBLE);

            } else {

                rateThisBar.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public void dataToViews(List<ComparableZipcode> data, String chartType){

        createEqualRegionViews(data, chartType);

    }

}
