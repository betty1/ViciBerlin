package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by betty on 25/11/15.
 */
public class GeoProvider {

    public static String plzFromLatLng(Context context, double lat, double lng){
        String plz = null;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address address = addresses.get(0);
            plz = address.getPostalCode();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return plz;
    }
}
