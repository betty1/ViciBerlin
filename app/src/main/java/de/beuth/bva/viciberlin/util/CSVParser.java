package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by betty on 03/11/15.
 */
public class CSVParser {

    final static String TAG = "CSVParser";

    public static float[] getFloatValuesForPLZ(Context context, String filename, String plz, int skip){

        boolean plzFound = false;
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
                    plzFound = true;

                    // fetch values
                    for(int i=0; i<numValues; i++){
                        values[i] = Float.parseFloat(lineSplit[i+1]);
                    }
                    break;

                }
            }
        } catch (Exception e) {

            Log.d(TAG, "Exceptions while parsing csv: " + e.getMessage());
            values = null;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exceptions while closing BufferedReader: " + e.getMessage());
                }
            }
        }

        if(!plzFound){
            values = null;
        }
        return values;
    }

    public static String getStringForPLZ(Context context, String filename, String plz){

        boolean plzFound = false;
        BufferedReader reader = null;
        String name = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");

                if(lineSplit[0].equals(plz)){
                    plzFound = true;

                    name = lineSplit[1];
                    break;

                }
            }
        } catch (Exception e) {

            Log.d(TAG, "Exceptions while parsing csv: " + e.getMessage());
            name = null;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exceptions while closing BufferedReader: " + e.getMessage());
                }
            }
        }

        if(!plzFound){
            name = null;
        }
        return name;
    }

    public static List<String> getPLZSuggestionForQuery(Context context, String query){

        BufferedReader reader = null;
        List<String> suggestions = new ArrayList<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("plz_names.csv"), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");
                String plz = lineSplit[0];
                String name = lineSplit[1];

                if(plz.startsWith(query)){
                    suggestions.add(plz + " " + name);
                } else if(name.toLowerCase().startsWith(query.toLowerCase())){
                    suggestions.add(plz + " " + name);
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

        return suggestions;
    }

    public static float[] getFloatValuesForPLZ(Context context, String filename, String plz) {
        return getFloatValuesForPLZ(context, filename, plz, 0);
    }

    }
