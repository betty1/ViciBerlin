package de.beuth.bva.viciberlin.ui;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.io.FileNotFoundException;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.CustomMapView;
import de.beuth.bva.viciberlin.util.GeoProvider;

public class MainActivity extends AppCompatActivity implements CustomMapView.LocationPressListener {

    private static final String TAG = "MainActivity";
    @Bind(R.id.map)
    CustomMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupMap();
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

    private void setupMap(){
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.callOnClick();
        mapView.setMinZoomLevel(12);
        mapView.setMaxZoomLevel(20);

        MapView mv = new MapView(this);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(14);
        GeoPoint startPoint = new GeoPoint(52.5167, 13.3833);
        mapController.setCenter(startPoint);

        mapView.setPressListener(this);
        mapView.setScrollableAreaLimit(new BoundingBoxE6(52.673235, 13.762254, 52.338880, 13.089341));
    }

    @Override
    public void onLocationPress(double lat, double lng) {
        String plz = GeoProvider.plzFromLatLng(this, lat, lng);

        if(plz==null){
            Toast.makeText(this, "Bitte wähle einen anderen Ort in der Nähe.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
        intent.setAction(Constants.PLZ_INTENT);
        intent.putExtra(Constants.PLZ_EXTRA, plz);
        startActivity(intent);
    }
}
