package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.MatrixCursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.List;

import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.geo.GeoProvider;
import de.beuth.bva.viciberlin.geo.GoogleLocationProvider;
import de.beuth.bva.viciberlin.util.CSVParserForZipCodes;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.DataHandler;

/**
 * Created by betty on 04/03/16.
 */
public class BaseActivity extends AppCompatActivity implements GoogleLocationProvider.LocationListener {

    final String TAG = BaseActivity.class.getSimpleName();

    Resources res;

    // SEARCH VIEW
    SimpleCursorAdapter searchAdapter;
    SearchView searchView;
    List<String> suggestions;

    // LOCATION
    MenuItem locationItem;
    String zipCodeOfUser;
    GoogleLocationProvider locationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getResources();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        locationItem = menu.findItem(R.id.location_item);

        if (zipCodeOfUser == null) {
            enableLocationItem(false);
        }

        configureSearchView(menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    protected void startLocationClickIntent(String zip) {

        if (zip != null) {
            String zipName = DataHandler.fetchZipName(this, zip);

            Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
            intent.setAction(Constants.ZIP_INTENT);
            intent.putExtra(Constants.ZIP_EXTRA, zip);
            intent.putExtra(Constants.ZIP_NAME_EXTRA, zipName);
            intent.putExtra(Constants.ZIPOFUSER_EXTRA, true);

            startActivity(intent);
        } else {
            Toast.makeText(this, res.getString(R.string.activate_location_services), Toast.LENGTH_LONG).show();
        }


    }

    private void enableLocationItem(boolean enable) {
        if (locationItem != null) {
            if (enable) {
                locationItem.getIcon().setAlpha(255);
            } else {
                locationItem.getIcon().setAlpha(100);
            }
        }
    }

    private void configureSearchView(Menu menu) {

        // Set up adapter
        searchAdapter = new SimpleCursorAdapter(this,
                R.layout.suggestion_textview,
                null,
                new String[]{"zip"},
                new int[]{android.R.id.text1},
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

                // Get zip and name from clicked suggestion
                String selected = suggestions.get(position);
                String plz = selected.split(" ")[0];
                String name = selected.substring(selected.indexOf(plz) + plz.length() + 1);

                Intent intent = new Intent(getApplicationContext(), PLZActivity.class);
                intent.setAction(Constants.ZIP_INTENT);
                intent.putExtra(Constants.ZIP_EXTRA, plz);
                intent.putExtra(Constants.ZIP_NAME_EXTRA, name);
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
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "zip"});
        suggestions = CSVParserForZipCodes.getZipCodeSuggestionForQuery(this, query);
        for (int i = 0; i < suggestions.size(); i++) {
            cursor.addRow(new Object[]{i, suggestions.get(i)});
        }
        searchAdapter.changeCursor(cursor);
    }

    @Override
    public void onLocationUpdate(Location loc) {
        zipCodeOfUser = GeoProvider.zipFromLatLng(this, loc.getLatitude(), loc.getLongitude());
        Log.d(TAG, "New Location: " + zipCodeOfUser);

        if (zipCodeOfUser != null) {

            enableLocationItem(true);
            locationProvider.stopLocationUpdates();

        }
    }
}
