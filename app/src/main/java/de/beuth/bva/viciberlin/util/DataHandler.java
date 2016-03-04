package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.model.ComparableZipcode;
import de.beuth.bva.viciberlin.model.ZipCodeResult;

/**
 * Created by betty on 03/12/15.
 */
public class DataHandler {

    final String TAG = "DataHandler";

    Resources res;
    Context context;
    DataReceiver receiver;
    String zip;

    public DataHandler(Context context, DataReceiver receiver){

        this.context = context;
        this.receiver = receiver;
        res = context.getResources();
    }

    public void setZip(String zip){
        this.zip = zip;
    }

    public void fillCharts(){

        // AGE CHART
        ZipCodeResult age = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.AGE_FILE, zip);
        float[] ageValues = age.getValues();
        List<ComparableZipcode> ageMostEquals = age.getMostEquals();

        float[] ageAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.AGE_FILE);
        String[] ageLabels = new String[]{res.getString(R.string.to_12), "12-17", "18-34", "35-65", res.getString(R.string.from_65)};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, ageAverages, ageLabels, res.getString(R.string.years), res.getString(R.string.percent));
        receiver.dataToChart(ageAttrs, Constants.AGE_CHART, new int[]{DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN});
        receiver.dataToViews(ageMostEquals.subList(0, 5), Constants.AGE_CHART);
        // AGE CHART END

        // AGE HISTORY CHART
        ZipCodeResult ageHistory = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.AGE_HISTORY_FILE, zip);
        float[] ageHistoryValues = ageHistory.getValues();

        float[] ageHistoryAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.AGE_HISTORY_FILE);
        ChartAttributes ageHistoryAttrs = new ChartAttributes(ageHistoryValues, ageHistoryAverages, ageLabels, res.getString(R.string.years), res.getString(R.string.percent));
        receiver.dataToChart(ageHistoryAttrs, Constants.AGE_HISTORY_CHART, new int[]{DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN});
        // AGE HISTORY CHART END

        // GENDER CHART
        ZipCodeResult gender = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.GENDER_FILE, zip);
        float[] genderValues = gender.getValues();
//        List<String> genderMostEquals = gender.getMostEquals();

        float[] genderAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.GENDER_FILE);
        String[] genderLabels = new String[]{res.getString(R.string.male), res.getString(R.string.female)};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, genderAverages, genderLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(genderAttrs, Constants.GENDER_CHART, new int[]{DataReceiver.DARKBLUE, DataReceiver.BLUE});
        // GENDER CHART END

        // LOCATION CHART
        ZipCodeResult location = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.LOCATION_FILE, zip, 1);
        float[] locationValues = location.getValues();
        List<ComparableZipcode> locationMostEquals = location.getMostEquals();

        float[] locationAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.LOCATION_FILE, 1);
        String[] locationLabels = new String[]{res.getString(R.string.simple), res.getString(R.string.mid), res.getString(R.string.good)};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, locationAverages, locationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(locationAttrs, Constants.LOCATION_CHART, null);
        receiver.dataToViews(locationMostEquals.subList(0, 5), Constants.LOCATION_CHART);
        // LOCATION CHART END

        // DURATION CHART
        ZipCodeResult duration = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.DURATION_FILE, zip);
        float[] durationValues = duration.getValues();
        List<ComparableZipcode> durationMostEquals = duration.getMostEquals();

        float[] durationAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.DURATION_FILE);
        String[] durationLabels = new String[]{res.getString(R.string.less_than_5_years), res.getString(R.string.five_to_10_years), res.getString(R.string.more_than_10_years)};
        ChartAttributes durationAttrs = new ChartAttributes(durationValues, durationAverages, durationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(durationAttrs, Constants.DURATION_CHART, null);
        receiver.dataToViews(durationMostEquals.subList(0, 5), Constants.DURATION_CHART);
        // DURATION CHART END

        // FOREIGNERS CHART
        ZipCodeResult foreigners = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.FOREIGNERS_FILE, zip);
        float[] foreignersValues = foreigners.getValues();
        List<ComparableZipcode> foreignersMostEquals = foreigners.getMostEquals();

        float[] foreignersAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.FOREIGNERS_FILE);
        String[] foreignersLabels = new String[]{res.getString(R.string.foreign_residents)};
        ChartAttributes foreignersAttrs = new ChartAttributes(foreignersValues, foreignersAverages, foreignersLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(foreignersAttrs, Constants.FOREIGNERS_CHART, null);
        receiver.dataToViews(foreignersMostEquals.subList(0, 5), Constants.FOREIGNERS_CHART);
        // FOREIGNERS CHART END

        // FOREIGNERS HISTORY CHART
        ZipCodeResult foreignersHistory = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.FOREIGNERS_HISTORY_FILE, zip);
        float[] foreignersHistoryValues = foreignersHistory.getValues();

        float[] foreignersHistoryAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.FOREIGNERS_HISTORY_FILE);
        ChartAttributes foreignersHistoryAttrs = new ChartAttributes(foreignersHistoryValues, foreignersHistoryAverages, foreignersLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(foreignersHistoryAttrs, Constants.FOREIGNERS_HISTORY_CHART, null);
        // FOREIGNERS HISTORY CHART END

        // TOTAL MOST EQUAL
        List<List<ComparableZipcode>> mostEquals = new ArrayList<>();
        mostEquals.add(ageMostEquals);
        mostEquals.add(locationMostEquals);
        mostEquals.add(durationMostEquals);
        mostEquals.add(foreignersMostEquals);
        List<ComparableZipcode> mostEqualZipcodes = SortHelper.getTotalDeviations(mostEquals);

        if(mostEqualZipcodes.size() >= 3){
            receiver.dataToViews(mostEqualZipcodes.subList(0, 3), Constants.MOST_EQUAL_CHART);
            Collections.reverse(mostEqualZipcodes);
            receiver.dataToViews(mostEqualZipcodes.subList(0, 3), Constants.LESS_EQUAL_CHART);
        }
        // TOTAL MOST EQUAL END

//        area = CSVParserForZipCodes.getFloatValuesForZipCode(context, "area.csv", zip)[0];
    }

    public String fetchZipName(){
        String name = CSVParserForZipCodes.getNameForZipCode(context, Constants.NAMES_FILE, zip);
        return name;
    }

    public List<String> fetchTwitterHashtags(String result){

        List<String> hashtagList = new ArrayList<>();
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray statusesArray = jsonResult.getJSONArray("statuses");

            for(int i=0; i<statusesArray.length(); i++){
                JSONObject status = statusesArray.getJSONObject(i);
                JSONArray hashtags = status.getJSONObject("entities").getJSONArray("hashtags");

                for(int j=0; j<hashtags.length(); j++){
                    JSONObject hashtagObject = hashtags.getJSONObject(j);
                    String hashtag = hashtagObject.getString("text");
                    hashtagList.add(hashtag);

                    Log.d(TAG, "Found hashtag: " + hashtag);
                }
            }
        } catch (Exception e){
            Log.d(TAG, "Error parsing Google Region JSON");
        }

        List<String> sortedHashtags = SortHelper.sortListByFrequency(hashtagList);

        return sortedHashtags;
    }

    public int fetchYelpTotals(String result) {
        if(zip != null && !zip.equals("")){
            try {
                JSONObject jsonResult = new JSONObject(result);
                int total = jsonResult.getInt("total");

                return total;

            } catch (Exception e){
                Log.d(TAG, "Error parsing Yelp JSON");
            }
        }
        return 0;
    }

    public String[] fetchGoogleLatLong(JSONObject result) {
        String[] latLong = new String[2];
        try {
            JSONArray resultArray = result.getJSONArray("results");
            JSONObject geometry = resultArray.getJSONObject(0).getJSONObject("geometry");
            Log.d(TAG, "Found geometry: " + geometry);
            JSONObject location = geometry.getJSONObject("location");
            String lat = location.getString("lat");
            String lng = location.getString("lng");
            latLong[0] = lat;
            latLong[1] = lng;

        } catch (Exception e){
            Log.d(TAG, "Error parsing Google Lat Long JSON");
        }
        return latLong;
    }

    public int[] fetchRatingResults(JSONObject result) {
        int[] ratingValues = new int[4];
        try {
            String culture = result.getString("culture");
            String infrastructure = result.getString("infrastructure");
            String green = result.getString("green");
            String safety = result.getString("safety");

            ratingValues[0] = Integer.parseInt(culture);
            ratingValues[1] = Integer.parseInt(infrastructure);
            ratingValues[2] = Integer.parseInt(green);
            ratingValues[3] = Integer.parseInt(safety);

        } catch (Exception e){
            Log.d(TAG, "Error parsing rating values");
        }
        return ratingValues;
    }

    public static String fetchZipName(Context context, String customZip){
        String name = CSVParserForZipCodes.getNameForZipCode(context, Constants.NAMES_FILE, customZip);
        return name;
    }

    public interface DataReceiver {
        int PURPLE = R.color.graph_purple;
        int GREEN = R.color.graph_green;
        int BLUE = R.color.graph_blue;
        int DARKBLUE = R.color.graph_darkblue;
        int TRANSGRAY = R.color.graph_transgray;

        void dataToChart(ChartAttributes attrs, String chartType, int[] colors);
//        void dataToViews(List<String> data, String chartType);
        void dataToViews(List<ComparableZipcode> data, String chartType);
    }
}
