package de.beuth.bva.viciberlin.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.beuth.bva.viciberlin.model.ComparableZipcode;

/**
 * From http://stackoverflow.com/questions/15377859/sort-a-list-by-frequency-of-strings-in-it
 */
public class SortHelper {

    public static String TAG = "SortHelper";

    public static List sortListByFrequency(List<String> list){

        Map<String, Integer> map = new HashMap<String, Integer>();

        for (String s : list) {
            String ls = s.toLowerCase();
            if (map.containsKey(ls)) {
                map.put(ls, map.get(ls) + 1);
            } else {
                map.put(ls, 1);
            }
        }
        List<String> sortedList = new ArrayList<String> (map.keySet());
        Collections.sort(sortedList, new ValueComparator<String, Integer>(map));

        return sortedList;

    }

    static class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {

        Map<K, V> map;

        public ValueComparator(Map<K, V> base) {
            this.map = base;
        }

        @Override
        public int compare(K o1, K o2) {
            return map.get(o2).compareTo(map.get(o1));
        }
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

    public static List<ComparableZipcode> getTotalDeviations(List<List<ComparableZipcode>> zipcodeLists){
        List<ComparableZipcode> totalDeviationList = new ArrayList<>();

        List<String> zipcodeNames = new ArrayList<>();
        List<Float> deviations = new ArrayList<>();

        for(List<ComparableZipcode> list: zipcodeLists){

            for(ComparableZipcode zipcode: list){

                String zipcodeName = zipcode.getName();
                float deviation = zipcode.getDeviation();

                int index = zipcodeNames.indexOf(zipcodeName);

                // If not yet in list
                if(index == -1){
                    zipcodeNames.add(zipcodeName);
                    deviations.add(deviation);
                } else {
                    float deviationSum = deviations.get(index) + deviation;
                    deviations.set(index, deviationSum);
                }

            }

        }

        for(int i=0; i<zipcodeNames.size(); i++){
            ComparableZipcode totalDeviationObject = new ComparableZipcode(
                    zipcodeNames.get(i), deviations.get(i));
            totalDeviationList.add(totalDeviationObject);
        }

        Collections.sort(totalDeviationList);
        return totalDeviationList;
    }

}
