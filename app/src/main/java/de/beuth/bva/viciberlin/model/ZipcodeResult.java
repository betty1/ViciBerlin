package de.beuth.bva.viciberlin.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by betty on 07/12/15.
 */
public class ZipCodeResult {

    float[] values;
    List<ComparableZipcode> mostEquals;

    public ZipCodeResult(float[] values, List<ComparableZipcode> mostEquals) {
        this.values = values;
        this.mostEquals = mostEquals;
    }

    public float[] getValues() {
        return values;
    }

    public List<ComparableZipcode> getMostEquals() {
        Collections.sort(mostEquals);
        return mostEquals;
    }

}
