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

/**
 * From http://stackoverflow.com/questions/15377859/sort-a-list-by-frequency-of-strings-in-it
 */
public class SortListByFrequency {

    public static List sortByFreq(List<String> list){

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

}
