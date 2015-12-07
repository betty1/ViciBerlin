package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import de.beuth.bva.viciberlin.util.CSVParser;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.ui.util.CustomMapView;
import de.beuth.bva.viciberlin.util.GeoProvider;

public class MainActivity extends AppCompatActivity implements CustomMapView.LocationPressListener {

    private static final String TAG = "MainActivity";
    @Bind(R.id.map) CustomMapView mapView;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.left_drawer) LinearLayout drawerLinearLayout;
    @Bind(R.id.nav_login_icon) ImageButton loginIcon;
    @Bind(R.id.nav_info_icon) ImageButton infoIcon;

    private ActionBarDrawerToggle drawerToggle;
    private SimpleCursorAdapter searchAdapter;
    private SearchView searchView;
    List<String> suggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupMap();
        setupNavigationDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSuggestionsAdapter(searchAdapter);
        searchView.isIconified();
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        { return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
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

        final String[] from = new String[] {"plz"};
        final int[] to = new int[] {android.R.id.text1};
        searchAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

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

    private void populateAdapter(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{ BaseColumns._ID, "plz" });
        suggestions = CSVParser.getPLZSuggestionForQuery(this, query);
        for (int i=0; i<suggestions.size(); i++) {
            cursor.addRow(new Object[] {i, suggestions.get(i)});
        }
        searchAdapter.changeCursor(cursor);
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

    private class NavigationOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.nav_login_icon:
                    intent = new Intent(getApplicationContext(), PLZActivity.class);
                    intent.setAction(Constants.PLZ_INTENT);
                    intent.putExtra(Constants.PLZ_EXTRA, "10405");
                    break;
                case R.id.nav_info_icon:
                    intent = new Intent(getApplicationContext(), PLZActivity.class);
                    intent.setAction(Constants.PLZ_INTENT);
                    intent.putExtra(Constants.PLZ_EXTRA, "10409");
                    break;
            }
            if(intent != null){
                startActivity(intent);
            }
        }
    }
}
