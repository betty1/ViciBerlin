package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.beuth.bva.viciberlin.model.PLZResult;

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

    public static PLZResult fetchPLZResult(Context context, String filename, String plz, int skip){

        BufferedReader reader = null;
        float[] values = null;
        Map<String, float[]> otherValues = new HashMap<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");

                int numValues = lineSplit.length-skip-1;

                if(lineSplit[0].equals(plz)) {
                    // fetch values for selected plz
                    values = new float[numValues];
                    for(int i=0; i<numValues; i++){
                        values[i] = Float.parseFloat(lineSplit[i+1]);
                    }
                } else {
                    // save values for comparison
                    float[] tempValues = new float[numValues];
                    for(int i=0; i<numValues; i++){
                        tempValues[i] = Float.parseFloat(lineSplit[i+1]);
                    }
                    otherValues.put(lineSplit[0], tempValues);
                }
            }
        } catch (Exception e) {

            Log.d(TAG, "Exceptions while parsing csv: " + e.getMessage());
            return null;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exceptions while closing BufferedReader: " + e.getMessage());
                }
            }
        }
        List<String> mostEquals = mostEquals(values, otherValues, 5);

        if(values != null && mostEquals != null){
            return new PLZResult(values, mostEquals);
        }
        return null;
    }

    public static PLZResult fetchPLZResult(Context context, String filename, String plz) {
        return fetchPLZResult(context, filename, plz, 0);
    }

        private static List<String> mostEquals(float[] comparator, Map<String, float[]> valueList, int numberOfResults){
        if(comparator == null || valueList == null || numberOfResults < 1){
            return null;
        }

        List<Float> deviations = new ArrayList<>();
        List<String> plzValues = new ArrayList<>();
        for(String key : valueList.keySet()){

            float[] values = valueList.get(key);
            float deviation = getAverageDeviation(values, comparator);

            int listSize = deviations.size();
            if(listSize >= numberOfResults){
                deviations = deviations.subList(0, numberOfResults);
                plzValues = plzValues.subList(0, numberOfResults);
                if(deviations.get(numberOfResults-1) <= deviation){
                    continue;
                }
            }

            boolean notInList = true;
            for(int i=0; i<listSize; i++){
                if(i==numberOfResults){
                    break;
                }
                if(deviation < deviations.get(i)){
                    deviations.add(i, deviation);
                    plzValues.add(i, key);
                    notInList = false;
                    break;
                }
            }
            if(listSize < numberOfResults && notInList){
                deviations.add(deviation);
                plzValues.add(key);
            }

        }
        plzValues = plzValues.subList(0, numberOfResults);
        return plzValues;
    }

    private static float getAverageDeviation(float[] values, float[] compareValues){
        if(values.length != compareValues.length || values.length == 0){
            Log.d(TAG, "Couldnt compare values to get deviation");
            return -1;
        }
        float deviationSum = 0;
        for(int i=0; i<values.length; i++){
            float deviation = Math.abs(values[i] - compareValues[i]);
            deviationSum += deviation;
        }
        return Math.round((deviationSum / values.length)*10)/10f;
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
