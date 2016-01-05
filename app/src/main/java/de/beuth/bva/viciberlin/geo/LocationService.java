package de.beuth.bva.viciberlin.geo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by betty on 15/12/15.
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    final String TAG = "LocationProvider";

    final int INTERVAL = 10000;
    final int INTERVAL_FASTEST = 10000;
    Activity context;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    public LocationServiceListener listener;
    public Location previousBestLocation = null;

    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    Intent intent;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        setupGoogleApiClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Stop LocationService");
        stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public synchronized void setupGoogleApiClient() {
        Log.d(TAG, "Setup Client");

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(INTERVAL_FASTEST);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void stopLocationUpdates() {
        if(googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                Log.d(TAG, "Stopped Updates");
            }
        }
    }

    public void startLocationUpdates(){
        if(googleApiClient != null){
            if(googleApiClient.isConnected()){
                Log.d(TAG, "Start Updates");
                if(locationRequest == null){
                    createLocationRequest();
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }
    }

    public String getPostalCode(Location loc){

        if(loc == null) {
            return null;
        } else {

            // Get city name from coordinates
            String cityName = null;
            String plz = null;
            Geocoder geocoder = new Geocoder(context.getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                plz = addresses.get(0).getPostalCode();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return plz;

        }
    }

    // ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        Log.d(TAG, "GoogleAPICLient connected:" + googleApiClient.isConnected());
        // If LastLocation is available -> send to Listener, if not -> startLocationUpdates
        if (lastLocation != null) {
            listener.onLocationUpdate(lastLocation);
            Log.d(TAG, "Latitude: " + lastLocation.getLatitude() + ", Longitude: " + lastLocation.getLongitude());
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleAPICLient suspended");
    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleAPICLient failed");
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location change!");

        if(location != null){
//            if(isBetterLocation(location, previousBestLocation)) {
                String plz = getPostalCode(location);
                intent.putExtra("plz", plz);
                sendBroadcast(intent);
//            }
        }
    }

    // Listener Interface
    public interface LocationServiceListener {
        void onLocationUpdate(Location loc);
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}