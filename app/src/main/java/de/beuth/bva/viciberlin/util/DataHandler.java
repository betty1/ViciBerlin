package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.model.ZipCodeResult;

/**
 * Created by betty on 03/12/15.
 */
public class DataHandler {

    final String TAG = "DataHandler";

    Resources res;
    Context context;
    DataReceiver receiver;
    String plz;

    public DataHandler(Context context, DataReceiver receiver){

        this.context = context;
        this.receiver = receiver;
        res = context.getResources();
    }

    public void setPlz(String plz){
        this.plz = plz;
    }

    public void fillCharts(){

        ZipCodeResult age = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.AGE_FILE, plz);
        float[] ageValues = age.getValues();
        List<String> ageMostEquals = age.getMostEquals();

        float[] ageAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.AGE_FILE);
        String[] ageLabels = new String[]{res.getString(R.string.to_12), "12-17", "18-34", "35-65", res.getString(R.string.from_65)};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, ageAverages, ageLabels, res.getString(R.string.years), res.getString(R.string.percent));
        receiver.dataToChart(ageAttrs, Constants.AGE_CHART, new int[]{DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN});
        receiver.dataToViews(ageMostEquals, Constants.AGE_CHART);

//        HISTORY CHART
        ZipCodeResult ageHistory = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.AGE_HISTORY_FILE, plz);
        float[] ageHistoryValues = ageHistory.getValues();

        float[] ageHistoryAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.AGE_HISTORY_FILE);
        ChartAttributes ageHistoryAttrs = new ChartAttributes(ageHistoryValues, ageHistoryAverages, ageLabels, res.getString(R.string.years), res.getString(R.string.percent));
        receiver.dataToChart(ageHistoryAttrs, Constants.AGE_HISTORY_CHART, new int[]{DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN});
//        ***

        ZipCodeResult gender = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.GENDER_FILE, plz);
        float[] genderValues = gender.getValues();
//        List<String> genderMostEquals = gender.getMostEquals();

        float[] genderAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.GENDER_FILE);
        String[] genderLabels = new String[]{res.getString(R.string.male), res.getString(R.string.female)};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, genderAverages, genderLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(genderAttrs, Constants.GENDER_CHART, new int[]{DataReceiver.DARKBLUE, DataReceiver.BLUE});

        ZipCodeResult location = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.LOCATION_FILE, plz, 1);
        float[] locationValues = location.getValues();
        List<String> locationMostEquals = location.getMostEquals();

        float[] locationAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.LOCATION_FILE, 1);
        String[] locationLabels = new String[]{res.getString(R.string.simple), res.getString(R.string.mid), res.getString(R.string.good)};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, locationAverages, locationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(locationAttrs, Constants.LOCATION_CHART, null);
        receiver.dataToViews(locationMostEquals, Constants.LOCATION_CHART);

        ZipCodeResult duration = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.DURATION_FILE, plz);
        float[] durationValues = duration.getValues();
        List<String> durationMostEquals = duration.getMostEquals();

        float[] durationAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.DURATION_FILE);
        String[] durationLabels = new String[]{res.getString(R.string.less_than_5_years), res.getString(R.string.five_to_10_years), res.getString(R.string.more_than_10_years)};
        ChartAttributes durationAttrs = new ChartAttributes(durationValues, durationAverages, durationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(durationAttrs, Constants.DURATION_CHART, null);
        receiver.dataToViews(durationMostEquals, Constants.DURATION_CHART);

        ZipCodeResult foreigners = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.FOREIGNERS_FILE, plz);
        float[] foreignersValues = foreigners.getValues();
        List<String> foreignersMostEquals = foreigners.getMostEquals();

        float[] foreignersAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.FOREIGNERS_FILE);
        String[] foreignersLabels = new String[]{res.getString(R.string.foreign_residents)};
        ChartAttributes foreignersAttrs = new ChartAttributes(foreignersValues, foreignersAverages, foreignersLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(foreignersAttrs, Constants.FOREIGNERS_CHART, null);
        receiver.dataToViews(foreignersMostEquals, Constants.FOREIGNERS_CHART);

        ZipCodeResult foreignersHistory = CSVParserForZipCodes.fetchZipCodeResult(context, Constants.FOREIGNERS_HISTORY_FILE, plz);
        float[] foreignersHistoryValues = foreignersHistory.getValues();

        float[] foreignersHistoryAverages = CSVParserForZipCodes.fetchAverageResult(context, Constants.FOREIGNERS_HISTORY_FILE);
        ChartAttributes foreignersHistoryAttrs = new ChartAttributes(foreignersHistoryValues, foreignersHistoryAverages, foreignersLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(foreignersHistoryAttrs, Constants.FOREIGNERS_HISTORY_CHART, null);

//        area = CSVParserForZipCodes.getFloatValuesForZipCode(context, "area.csv", plz)[0];
    }

    public String fetchZipCodeName(){
        String name = CSVParserForZipCodes.getNameForZipCode(context, Constants.NAMES_FILE, plz);
        return name;
    }

    public String fetchZipCodeName(String customPlz){
        String name = CSVParserForZipCodes.getNameForZipCode(context, Constants.NAMES_FILE, customPlz);
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

        List<String> sortedHashtags = SortListByFrequency.sortByFreq(hashtagList);

        return sortedHashtags;
    }

    public int fetchYelpTotals(String result) {
        if(plz != null && !plz.equals("")){
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

    public interface DataReceiver {
        int PURPLE = R.color.graph_purple;
        int GREEN = R.color.graph_green;
        int BLUE = R.color.graph_blue;
        int DARKBLUE = R.color.graph_darkblue;
        int TRANSGRAY = R.color.graph_transgray;

        void dataToChart(ChartAttributes attrs, String chartType, int[] colors);
        void dataToViews(List<String> data, String chartType);
    }
}
