package de.beuth.bva.viciberlin.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.geo.GeoProvider;
import de.beuth.bva.viciberlin.geo.GoogleLocationProvider;
import de.beuth.bva.viciberlin.ui.util.CustomMapView;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.DataHandler;

public class MainActivity extends BaseActivity implements CustomMapView.LocationPressListener, GeoProvider.Listener {

    final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.map)
    CustomMapView mapView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.nav_login_icon)
    ImageButton loginIcon;
    @Bind(R.id.nav_info_icon)
    ImageButton infoIcon;
    @Bind(R.id.map_progress_layer)
    FrameLayout mapProgressLayer;

    private ActionBarDrawerToggle drawerToggle;

    private boolean isProcessingMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        locationProvider = new GoogleLocationProvider(this, this);
        locationProvider.setupGoogleApiClient();

        setupMap();
        setupNavigationDrawer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (zipCodeOfUser == null) {
            locationProvider.setupGoogleApiClient();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationProvider.stopLocationUpdates();

        mapView.getTileProvider().clearTileCache();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.location_item) {

            startLocationClickIntent(zipCodeOfUser);

            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void setupNavigationDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle
                (this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationOnClickListener navListener = new NavigationOnClickListener();
        loginIcon.setOnClickListener(navListener);
        infoIcon.setOnClickListener(navListener);

    }

    private void setupMap() {
        IMapController mapController = mapView.getController();

        // Touch handling
        mapView.callOnClick();
        mapView.setPressListener(this);
        mapView.setMultiTouchControls(true);

        // Tile Source Factory
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        // Zoom restrictions
        mapController.setZoom(14);
        mapView.setMinZoomLevel(12);
        mapView.setMaxZoomLevel(20);

        // Centered point
        GeoPoint startPoint = new GeoPoint(Constants.BERLIN_LAT, Constants.BERLIN_LNG);
        mapController.setCenter(startPoint);

        // Bounding box
        mapView.setScrollableAreaLimit(new BoundingBoxE6(Constants.BERLIN_NORTH, Constants.BERLIN_EAST,
                Constants.BERLIN_SOUTH, Constants.BERLIN_WEST));
    }

    private String getTwitterUserId() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_VICI, MODE_PRIVATE);
        String twitterUserId = prefs.getString(Constants.TWITTER_USERID, null);
        String twitterUsername = prefs.getString(Constants.TWITTER_USERNAME, null);
        Log.i(TAG, "twitterUsername is " + twitterUsername);
        Log.i(TAG, "twitterUserId is " + twitterUserId);
        return twitterUserId;
    }

    @Override
    public void onLocationPress(double lat, double lng) {

        // Check whether a map request is already in process
        if (!isProcessingMap) {
            isProcessingMap = true;

            mapProgressLayer.setVisibility(View.VISIBLE);

            GeoProvider.zipFromLatLng(this, lat, lng, this);
        }
    }

    @Override
    public void onZipCodeResult(String zip) {

        mapProgressLayer.setVisibility(View.INVISIBLE);
        isProcessingMap = false;

        if (zip != null) {

            if (zip == GeoProvider.NO_SERVER_RESPONSE) {
                Toast.makeText(this, res.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                return;
            } else if (zip == GeoProvider.NO_ZIP_AVAILABLE) {
                Toast.makeText(this, res.getString(R.string.no_available_zipcode), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get name of zipcode region
            String name = DataHandler.fetchZipName(this, zip);

            if (name == null) {
                Toast.makeText(this, res.getString(R.string.no_berlin_zipcode), Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
            intent.setAction(Constants.ZIP_INTENT);
            intent.putExtra(Constants.ZIP_EXTRA, zip);
            intent.putExtra(Constants.ZIP_NAME_EXTRA, name);
            startActivity(intent);

        }
    }

    private class NavigationOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.nav_login_icon:
                    if (getTwitterUserId() != null) {
                        intent = new Intent(getApplicationContext(), LogoutActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                    }
                    break;
                case R.id.nav_info_icon:
                    onBackPressed();
                    return;
            }
            if (intent != null) {
                startActivity(intent);
            }
        }
    }
}
