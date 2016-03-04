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

import de.beuth.bva.viciberlin.model.ComparableZipcode;
import de.beuth.bva.viciberlin.model.ZipCodeResult;

/**
 * Created by betty on 03/11/15.
 */
public class CSVParserForZipCodes {

    final static String TAG = CSVParserForZipCodes.class.getSimpleName();

    /**
     * Collects values from a csv file for a specific zip code or special line (e.g. average)
     * and returns them in form of a ZipCodeResult object
     *
     * @param context
     * @param filename csv file name
     * @param lookUp   a specific zip code or a line title constant like 'Constants.AVERAGE_LINE'
     * @return ZipCodeResult object with values and (if a zip code was looked up) with most equal zip codes
     */
    public static ZipCodeResult fetchZipCodeResult(Context context, String filename, String lookUp, int skip) {

        BufferedReader reader = null;
        float[] valuesOfZipCode = null;
        Map<String, float[]> valuesOfOtherZipCodes = new HashMap<>();

        // Check if looking for a zip code or for a special line name
        boolean lookUpZipCode = !lookUp.equals(Constants.AVERAGE_LINE);

        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

            String line;

            // loop until end of file
            while ((line = reader.readLine()) != null) {

                // lineSplit[0] = zipCode of line, lineSplit[1+...] = values
                String[] lineSplit = line.split(";");

                String lineName = lineSplit[0];
                int attributeCount = lineSplit.length - 1 - skip;

                // Check if linename is a zipcode or one of the special values
                switch (lineName) {
                    case Constants.ZIPCODE_LINE:
                        continue;
                    case Constants.AVERAGE_LINE:
                        if (lookUpZipCode) {
                            continue;
                        }
                    default:

                        if (lineName.equals(lookUp)) {

                            // save values for selected zipCode or average
                            valuesOfZipCode = new float[attributeCount];

                            for (int i = 0; i < attributeCount; i++) {
                                valuesOfZipCode[i] = Float.parseFloat(lineSplit[1 + i]);
                            }

                        } else {

                            // don't look for equal regions if just looking up average values
                            if (lookUpZipCode) {

                                // save values of other zipcodes for comparison
                                float[] valuesOfOtherZipCode = new float[attributeCount];

                                for (int i = 0; i < attributeCount; i++) {
                                    valuesOfOtherZipCode[i] = Float.parseFloat(lineSplit[1 + i]);
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

        List<ComparableZipcode> otherZipcodes = new ArrayList<>();

        // Get list of most equals if actually looking for a zip code (not e.g. the average)
        if (lookUpZipCode) {
            otherZipcodes = getZipcodeDeviations(valuesOfZipCode, valuesOfOtherZipCodes);
        }

        return new ZipCodeResult(valuesOfZipCode, otherZipcodes);
    }

    /**
     * Collects values from a csv file for a specific zip code or special line (e.g. average)
     * and returns them in form of a ZipCodeResult object
     *
     * @param context
     * @param filename csv file name
     * @param lookUp   a specific zip code or a line title constant like 'Constants.AVERAGE_LINE'
     * @return ZipCodeResult object with values and (if a zip code was looked up) with most equal zip codes
     */
    public static ZipCodeResult fetchZipCodeResult(Context context, String filename, String lookUp) {
        return fetchZipCodeResult(context, filename, lookUp, 0);
    }

    /**
     * Creates a list of zip codes with the lowest average deviation from values of one specific zip code
     */
    public static List<ComparableZipcode> getZipcodeDeviations(float[] values, Map<String, float[]> valuesToCompare) {

        // Refuse incorrect requests
        if (values == null || valuesToCompare == null) {
            return null;
        }
        List<ComparableZipcode> comparableZipcodes = new ArrayList<>();

        for (String name : valuesToCompare.keySet()) {

            float[] valuesOfOtherZipCode = valuesToCompare.get(name);
            float deviation = getAverageDeviation(valuesOfOtherZipCode, values);

            ComparableZipcode comparableZipcode = new ComparableZipcode(name, deviation);

            comparableZipcodes.add(comparableZipcode);
        }

        return comparableZipcodes;
    }

    /**
     * Gets the average values from a csv file
     *
     * @param context
     * @param filename csv file name
     * @param skip     nr of columns to skip at the end of the line
     * @return array of average float values from csv
     */
    public static float[] fetchAverageResult(Context context, String filename, int skip) {
        ZipCodeResult result = fetchZipCodeResult(context, filename, Constants.AVERAGE_LINE, skip);
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
     * Compares two float value arrays and returns the average deviation
     *
     * @param values
     * @param compareValues
     * @return Average deviation of values from 'values' and 'compareValues'
     */
    private static float getAverageDeviation(float[] values, float[] compareValues) {
        if (values.length != compareValues.length || values.length == 0) {
            Log.d(TAG, "Couldnt compare values to get deviation");
            return -1;
        }
        float deviationSum = 0;
        for (int i = 0; i < values.length; i++) {
            float deviation = Math.abs(values[i] - compareValues[i]);
            deviationSum += deviation;
        }
        return Math.round((deviationSum / values.length) * 10) / 10f;
    }

    /**
     * Gets name of zip code area from a csv file
     *
     * @param context
     * @param filename csv file name
     * @param zipCode  zipcode to look up name for
     * @return name of zip code area
     */
    public static String getNameForZipCode(Context context, String filename, String zipCode) {

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

                if (lineSplit[0].equals(zipCode)) {
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

        if (!zipCodeFound) {
            name = null;
        }
        return name;
    }

    /**
     * Gets a list of zip codes representation Strings that match the current query
     *
     * @param context
     * @param query   String e.g. entered by user
     * @return list of all zip codes with their names that match the query
     */
    public static List<String> getZipCodeSuggestionForQuery(Context context, String query) {

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

                if (zipCode.equals(Constants.ZIPCODE_LINE)) {
                    continue;
                }

                if (zipCode.startsWith(query)) {
                    suggestions.add(zipCode + " " + name);
                } else if (name.toLowerCase().startsWith(query.toLowerCase())) {
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
