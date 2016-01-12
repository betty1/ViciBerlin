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

import de.beuth.bva.viciberlin.model.ZipCodeResult;

/**
 * Created by betty on 03/11/15.
 */
public class CSVParserForZipCodes {

    final static String TAG = "CSVParserForZipCodes";

    /**
     * Collects values from a csv file for a specific zip code or special line (e.g. average)
     * and returns them in form of a ZipCodeResult object
     *
     * @param context
     * @param filename csv file name
     * @param lookUp a specific zip code or a line title constant like 'Constants.AVERAGE_TITLE'
     * @return ZipCodeResult object with values and (if a zip code was looked up) with most equal zip codes
     */
    public static ZipCodeResult fetchZipCodeResult(Context context, String filename, String lookUp, int skip){

        final int MOST_EQUAL_LIST_SIZE = 5;

        BufferedReader reader = null;
        float[] valuesOfZipCode = null;
        Map<String, float[]> valuesOfOtherZipCodes = new HashMap<>();

        // Check if looking for a zip code or for a special line name
        boolean lookUpZipCode = !lookUp.equals(Constants.AVERAGE_TITLE);

        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            String line;

            // loop until end of file
            while ((line = reader.readLine()) != null) {

                // lineSplit[0] = zipCode of line, lineSplit[1+...] = values
                String[] lineSplit = line.split(";");

                String lineName = lineSplit[0];
                int attributeCount = lineSplit.length-1-skip;

                // Check if linename is a zipcode or one of the special values
                switch(lineName){
                    case Constants.ZIPCODE_TITLE:
                        continue;
                    case Constants.AVERAGE_TITLE:
                        if(lookUpZipCode){
                            continue;
                        }
                    default:

                        if(lineName.equals(lookUp)) {

                            // save values for selected zipCode or average
                            valuesOfZipCode = new float[attributeCount];

                            for(int i = 0; i < attributeCount; i++){
                                valuesOfZipCode[i] = Float.parseFloat(lineSplit[1+i]);
                            }

                        } else {

                            // don't look for equal regions if just looking up average values
                            if(lookUpZipCode){

                                // save values of other zipcodes for comparison
                                float[] valuesOfOtherZipCode = new float[attributeCount];

                                for(int i=0; i<attributeCount; i++){
                                    valuesOfOtherZipCode[i] = Float.parseFloat(lineSplit[1+i]);
                                }
                                valuesOfOtherZipCodes.put(lineName, valuesOfOtherZipCode);
                            }
                        }

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

        List<String> mostEquals = null;
        // Get list of most equals if actually looking for a zip code (not e.g. the average)
        if(lookUpZipCode){
            mostEquals = mostEquals(valuesOfZipCode, valuesOfOtherZipCodes, MOST_EQUAL_LIST_SIZE);
        }

        return new ZipCodeResult(valuesOfZipCode, mostEquals);
    }

    /**
     * Collects values from a csv file for a specific zip code or special line (e.g. average)
     * and returns them in form of a ZipCodeResult object
     *
     * @param context
     * @param filename csv file name
     * @param lookUp a specific zip code or a line title constant like 'Constants.AVERAGE_TITLE'
     * @return ZipCodeResult object with values and (if a zip code was looked up) with most equal zip codes
     */
    public static ZipCodeResult fetchZipCodeResult(Context context, String filename, String lookUp) {
        return fetchZipCodeResult(context, filename, lookUp, 0);
    }

    /**
     * Gets the average values from a csv file
     *
     * @param context
     * @param filename csv file name
     * @param skip nr of columns to skip at the end of the line
     * @return array of average float values from csv
     */
    public static float[] fetchAverageResult(Context context, String filename, int skip){
        ZipCodeResult result = fetchZipCodeResult(context, filename, Constants.AVERAGE_TITLE, skip);
        return result.getValues();
    }

    /**
     * Gets the average values from a csv file
     *
     * @param context
     * @param filename csv file name
     * @return array of average float values from csv
     */
    public static float[] fetchAverageResult(Context context, String filename) {
        return fetchAverageResult(context, filename, 0);
    }

    /**
     * Creates a list of zip codes with the lowest average deviation from values of one specific zip code
     *
     * @param valuesOfZipCode values from specific zip code to be compared with the others
     * @param valuesOfOtherZipCodes names and values from other zip codes
     * @param numberOfResults max size of returned list
     * @return list of most equal zip codes
     */
    private static List<String> mostEquals(float[] valuesOfZipCode, Map<String, float[]> valuesOfOtherZipCodes, int numberOfResults){

        // Refuse incorrect requests
        if(valuesOfZipCode == null || valuesOfOtherZipCodes == null || numberOfResults < 1){
            return null;
        }

        List<Float> deviations = new ArrayList<>();
        List<String> mostEqualZipCodes = new ArrayList<>();

        for(String nameOfOtherZipCode : valuesOfOtherZipCodes.keySet()){

            float[] valuesOfOtherZipCode = valuesOfOtherZipCodes.get(nameOfOtherZipCode);
            float deviation = getAverageDeviation(valuesOfOtherZipCode, valuesOfZipCode);

            int listSize = deviations.size();

            // Check if list is full
            if(listSize >= numberOfResults){
                // shrink list
                deviations = deviations.subList(0, numberOfResults);
                mostEqualZipCodes = mostEqualZipCodes.subList(0, numberOfResults);

                // If current deviation value if higher than last value in list, continue with next zipcode
                if(deviations.get(numberOfResults-1) <= deviation){
                    continue;
                }
            }

            // List is not full yet
            boolean notInList = true;
            for(int i=0; i<listSize; i++){
                if(i==numberOfResults){
                    break;
                }
                // sort value in
                if(deviation < deviations.get(i)){
                    deviations.add(i, deviation);
                    mostEqualZipCodes.add(i, nameOfOtherZipCode);
                    notInList = false;
                    break;
                }
            }
            // if value is higher than all others, but list is not full, add to end
            if(listSize < numberOfResults && notInList){
                deviations.add(deviation);
                mostEqualZipCodes.add(nameOfOtherZipCode);
            }

        }

        // shrink list again
        mostEqualZipCodes = mostEqualZipCodes.subList(0, numberOfResults);
        return mostEqualZipCodes;
    }

    /**
     * Compares two float value arrays and returns the average deviation
     * @param values
     * @param compareValues
     * @return Average deviation of values from 'values' and 'compareValues'
     */
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

    /**
     * Gets name of zip code area from a csv file
     * @param context
     * @param filename csv file name
     * @param zipCode zipcode to look up name for
     * @return name of zip code area
     */
    public static String getNameForZipCode(Context context, String filename, String zipCode){

        boolean zipCodeFound = false;
        BufferedReader reader = null;
        String name = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");

                if(lineSplit[0].equals(zipCode)){
                    zipCodeFound = true;

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

        if(!zipCodeFound){
            name = null;
        }
        return name;
    }

    /**
     * Gets a list of zip codes representation Strings that match the current query
     * @param context
     * @param query String e.g. entered by user
     * @return list of all zip codes with their names that match the query
     */
    public static List<String> getZipCodeSuggestionForQuery(Context context, String query){

        BufferedReader reader = null;
        List<String> suggestions = new ArrayList<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(Constants.NAMES_FILE), "UTF-8"));

            // loop until end of file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");
                String zipCode = lineSplit[0];
                String name = lineSplit[1];

                if(name.equals(Constants.ZIPCODE_TITLE)){
                    continue;
                }

                if(zipCode.startsWith(query)){
                    suggestions.add(zipCode + " " + name);
                } else if(name.toLowerCase().startsWith(query.toLowerCase())){
                    suggestions.add(zipCode + " " + name);
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
    }
