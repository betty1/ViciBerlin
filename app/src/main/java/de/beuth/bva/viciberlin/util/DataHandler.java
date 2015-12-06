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

        float[] ageValues = CSVParser.getFloatValuesForPLZ(context, "age.csv", plz);
        float[] ageAverages = CSVParser.getFloatValuesForPLZ(context, "age.csv", "average");
        String[] ageLabels = new String[]{res.getString(R.string.to_12), "12-17", "18-34", "35-65", res.getString(R.string.from_65)};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, ageAverages, ageLabels, res.getString(R.string.years), res.getString(R.string.percent));
        receiver.dataToChart(ageAttrs, DataReceiver.AGE_CHART, new int[]{DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN, DataReceiver.PURPLE, DataReceiver.GREEN});

        float[] genderValues = CSVParser.getFloatValuesForPLZ(context, "gender.csv", plz);
        float[] genderAverages = CSVParser.getFloatValuesForPLZ(context, "gender.csv", "average");
        String[] genderLabels = new String[]{res.getString(R.string.male), res.getString(R.string.female)};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, genderAverages, genderLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(genderAttrs, DataReceiver.GENDER_CHART, new int[]{DataReceiver.DARKBLUE, DataReceiver.BLUE});

        float[] locationValues = CSVParser.getFloatValuesForPLZ(context, "wohnlage.csv", plz, 1);
        float[] locationAverages = CSVParser.getFloatValuesForPLZ(context, "wohnlage.csv", "average", 1);
        String[] locationLabels = new String[]{res.getString(R.string.simple), res.getString(R.string.mid), res.getString(R.string.good)};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, locationAverages, locationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(locationAttrs, DataReceiver.LOCATION_CHART, null);

        float[] durationValues = CSVParser.getFloatValuesForPLZ(context, "wohndauer.csv", plz);
        float[] durationAverages = CSVParser.getFloatValuesForPLZ(context, "wohndauer.csv", "average");
        String[] durationLabels = new String[]{res.getString(R.string.less_than_5_years), res.getString(R.string.five_to_10_years), res.getString(R.string.more_than_10_years)};
        ChartAttributes durationAttrs = new ChartAttributes(durationValues, durationAverages, durationLabels, "", res.getString(R.string.percent));
        receiver.dataToChart(durationAttrs, DataReceiver.DURATION_CHART, null);

//        area = CSVParser.getFloatValuesForPLZ(context, "area.csv", plz)[0];
    }

    public String fetchPLZName(){
        String name = CSVParser.getStringForPLZ(context, "plz_names.csv", plz);
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

    public String[] fetchGoogleLatLong(String result) {
        String[] latLong = new String[2];
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray resultArray = jsonResult.getJSONArray("results");
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

    public interface DataReceiver {
        int PURPLE = R.color.graph_purple;
        int GREEN = R.color.graph_green;
        int BLUE = R.color.graph_blue;
        int DARKBLUE = R.color.graph_darkblue;
        int TRANSGRAY = R.color.graph_transgray;

        String AGE_CHART = "ageChart";
        String GENDER_CHART = "genderChart";
        String LOCATION_CHART = "locationChart";
        String DURATION_CHART = "durationChart";
        void dataToChart(ChartAttributes attrs, String chartType, int[] colors);
    }
}
