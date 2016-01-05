//package de.beuth.bva.viciberlin.geo;
//
//import android.app.Activity;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
////
////import com.google.android.gms.common.ConnectionResult;
////import com.google.android.gms.common.api.GoogleApiClient;
////import com.google.android.gms.location.LocationListener;
////import com.google.android.gms.location.LocationRequest;
////import com.google.android.gms.location.LocationServices;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//
//public class GoogleLocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//
//    final String TAG = "LocationProvider";
//
//    final int INTERVAL = 10000;
//    final int INTERVAL_FASTEST = 10000;
//    Activity context;
//    GoogleApiClient googleApiClient;
//    LocationListener listener;
//    LocationRequest locationRequest;
//
//    public GoogleLocationProvider(Activity context, LocationListener listener){
//        this.context = context;
//        this.listener = listener;
//    }
//
//    public synchronized void setupGoogleApiClient() {
//        Log.d(TAG, "Setup Client");
//
//        googleApiClient = new GoogleApiClient.Builder(context)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();
//    }
//
//    protected void createLocationRequest() {
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(INTERVAL);
//        locationRequest.setFastestInterval(INTERVAL_FASTEST);
//        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
//    }
//
//    public void stopLocationUpdates() {
//        if(googleApiClient != null) {
//            if (googleApiClient.isConnected()) {
//                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//                Log.d(TAG, "Stopped Updates");
//            }
//        }
//    }
//
//    public void startLocationUpdates(){
//        if(googleApiClient != null){
//            if(googleApiClient.isConnected()){
//                Log.d(TAG, "Start Updates");
//                if(locationRequest == null){
//                    createLocationRequest();
//                }
//                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//            }
//        }
//    }
//
//    public String[] getCityName(Location loc){
//
//        if(loc == null) {
//            return null;
//        } else {
//
//            // Get city name from coordinates
//            String cityName = null;
//            String plz = null;
//            Geocoder geocoder = new Geocoder(context.getBaseContext(), Locale.getDefault());
//            List<Address> addresses;
//            try {
//                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
//                cityName = addresses.get(0).getLocality();
//                plz = addresses.get(0).getPostalCode();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            return new String[]{cityName, plz};
//
//        }
//    }
//
//    // ConnectionCallbacks
//    @Override
//    public void onConnected(Bundle bundle) {
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//
//        Log.d(TAG, "GoogleAPICLient connected:" + googleApiClient.isConnected());
//        // If LastLocation is available -> send to Listener, if not -> startLocationUpdates
//        if (lastLocation != null) {
//            listener.onLocationUpdate(lastLocation);
//            Log.d(TAG, "Latitude: " + lastLocation.getLatitude() + ", Longitude: " + lastLocation.getLongitude());
//        } else {
//            startLocationUpdates();
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.d(TAG, "GoogleAPICLient suspended");
//    }
//
//    // OnConnectionFailedListener
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.d(TAG, "GoogleAPICLient failed");
//    }
//
//    // LocationListener
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.d(TAG, "Location change");
//
//        if(location != null){
//            listener.onLocationUpdate(location);
//        }
//    }
//
//    // Listener Interface
//    public interface LocationListener {
//        void onLocationUpdate(Location loc);
//    }
//}
//
