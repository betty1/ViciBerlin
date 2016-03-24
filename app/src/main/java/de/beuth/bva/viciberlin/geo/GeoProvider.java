package de.beuth.bva.viciberlin.geo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by betty on 25/11/15.
 */
public class GeoProvider {

    final public static String NO_SERVER_RESPONSE = "noServerResponse";
    final public static String NO_ZIP_AVAILABLE = "noZipAvailable";

    public static void zipFromLatLng(final Context context, final double lat, final double lng, final Listener listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return zipFromLatLng(context, lat, lng);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                System.out.println("String = " + result);

                if (listener != null) {
                    listener.onZipCodeResult(result);
                }
            }
        }.execute();
    }


    public static String zipFromLatLng(Context context, double lat, double lng) {
        String plz;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address address = addresses.get(0);
            plz = address.getPostalCode();
        } catch (IOException e) {
            e.printStackTrace();
            return NO_SERVER_RESPONSE;
        } catch (Exception e) {
            return NO_ZIP_AVAILABLE;
        }

        if (plz == null) {
            return NO_ZIP_AVAILABLE;
        }

        return plz;
    }

    public interface Listener {
        void onZipCodeResult(String zip);
    }
}
