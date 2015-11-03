package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by betty on 03/11/15.
 */
public class CSVParser {

    final static String TAG = "CSVParser";

    public static float[] getFloatValuesForPLZ(Context context, String filename, String plz, int skip){

        BufferedReader reader = null;
        float[] values = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");

                int numValues = lineSplit.length-skip-1;

                values = new float[numValues];

                if(lineSplit[0].equals(plz)){

                    // fetch values
                    for(int i=0; i<numValues; i++){
                        values[i] = Float.parseFloat(lineSplit[i+1]);
                        Log.d(TAG, "parsedFloat: " + values[i]);
                    }
                    break;

                }
            }
        } catch (Exception e) {

            Log.d(TAG, "Exceptions while parsing csv: " + e.getMessage());

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exceptions while closing BufferedReader: " + e.getMessage());
                }
            }
        }

        return values;
    }

    public static float[] getFloatValuesForPLZ(Context context, String filename, String plz) {
        return getFloatValuesForPLZ(context, filename, plz, 0);
    }

    }
