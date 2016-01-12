package de.beuth.bva.viciberlin.model;

import java.util.List;

/**
 * Created by betty on 07/12/15.
 */
public class ZipCodeResult {

    float[] values;
    List<String> mostEquals;

    public ZipCodeResult(float[] values, List<String> mostEquals) {
        this.values = values;
        this.mostEquals = mostEquals;
    }

    public float[] getValues() {
        return values;
    }

    public List<String> getMostEquals() {
        return mostEquals;
    }



}
