package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.location.Location;
import android.provider.BaseColumns;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.geo.GoogleLocationProvider;
import de.beuth.bva.viciberlin.util.CSVParserForZipCodes;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.ui.util.CustomMapView;
import de.beuth.bva.viciberlin.geo.GeoProvider;

public class MainActivity extends AppCompatActivity implements CustomMapView.LocationPressListener, GoogleLocationProvider.LocationListener {

    private static final String TAG = "MainActivity";
    @Bind(R.id.map) CustomMapView mapView;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.nav_login_icon) ImageButton loginIcon;
    @Bind(R.id.nav_info_icon) ImageButton infoIcon;
    @Bind(R.id.map_progress_layer) FrameLayout mapProgressLayer;

    private ActionBarDrawerToggle drawerToggle;
    private SimpleCursorAdapter searchAdapter;
    private SearchView searchView;
    List<String> suggestions;
    Resources res;

    // Location
    GoogleLocationProvider locationProvider;
    String zipCodeOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        res = getResources();

        locationProvider = new GoogleLocationProvider(this, this);
        locationProvider.setupGoogleApiClient();

        setupMap();
        setupNavigationDrawer();
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        configureSearchView(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.location_item){

            if(zipCodeOfUser != null){
                Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
                intent.setAction(Constants.PLZ_INTENT);
                intent.putExtra(Constants.PLZ_EXTRA, zipCodeOfUser);
                intent.putExtra(Constants.ZIPOFUSER_EXTRA, true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bitte aktivieren Sie Location Services und warten Sie einen Moment, damit Ihr Standort ermittelt werden kann.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item))
        { return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if(!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void setupNavigationDrawer(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle
                (this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {};

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationOnClickListener navListener = new NavigationOnClickListener();
        loginIcon.setOnClickListener(navListener);
        infoIcon.setOnClickListener(navListener);

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
                populateAdapter(s);
                return false;
            }
        });

        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setDropDownBackgroundResource(R.color.graph_transdarkblue);
    }

    private void populateAdapter(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{ BaseColumns._ID, "plz" });
        suggestions = CSVParserForZipCodes.getZipCodeSuggestionForQuery(this, query);
        for (int i=0; i<suggestions.size(); i++) {
            cursor.addRow(new Object[] {i, suggestions.get(i)});
        }
        searchAdapter.changeCursor(cursor);
    }

    private void setupMap(){
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.callOnClick();
        mapView.setMinZoomLevel(12);
        mapView.setMaxZoomLevel(20);

        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(14);
        GeoPoint startPoint = new GeoPoint(52.5167, 13.3833);
        mapController.setCenter(startPoint);

        mapView.setPressListener(this);
        mapView.setScrollableAreaLimit(new BoundingBoxE6(52.673235, 13.762254, 52.338880, 13.089341));
    }

    private String getTwitterUserId(){
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_VICI, MODE_PRIVATE);
        String twitterUserId = prefs.getString(Constants.TWITTER_USERID, null);
        String twitterUsername = prefs.getString(Constants.TWITTER_USERNAME, null);
        Log.i(TAG, "twitterUsername is " + twitterUsername);
        Log.i(TAG, "twitterUserId is " + twitterUserId);
        return twitterUserId;
    }

    @Override
    public void onLocationPress(double lat, double lng) {

        String plz = GeoProvider.plzFromLatLng(this, lat, lng);

        if(plz == GeoProvider.NO_SERVER_RESPONSE){
            Toast.makeText(this, res.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        } else if(plz == GeoProvider.NO_ZIP_AVAILABLE){
            Toast.makeText(this, res.getString(R.string.no_available_zipcode), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
        intent.setAction(Constants.PLZ_INTENT);
        intent.putExtra(Constants.PLZ_EXTRA, plz);
        startActivity(intent);
    }

    @Override
    public void onLocationUpdate(Location loc) {
        zipCodeOfUser = GeoProvider.plzFromLatLng(this, loc.getLatitude(), loc.getLongitude());
        Log.d(TAG, "New Location: " + zipCodeOfUser);

        if(zipCodeOfUser != null){
            locationProvider.stopLocationUpdates();
        }
    }


    private class NavigationOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.nav_login_icon:
                    if(getTwitterUserId() != null){
                        intent = new Intent(getApplicationContext(), LogoutActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                    }
                    break;
                case R.id.nav_info_icon:
                    onBackPressed();
                    return;
            }
            if(intent != null){
                startActivity(intent);
            }
        }
    }
}
