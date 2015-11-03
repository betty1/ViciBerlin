package de.beuth.bva.viciberlin.model;

import java.util.List;

import lecho.lib.hellocharts.model.SubcolumnValue;

/**
 * Created by betty on 03/11/15.
 */
public class ChartAttributes {

    public float[] values;
    public float[] subValues;

    public String axisXName;
    public String axisYName;

    public String[] axisXLabels;

    public ChartAttributes(float[] values, float[] subValues, String[] axisXLabels, String axisXName, String axisYName){
        this.values = values;
        if(subValues != null){
            this.subValues = subValues;
        } else {
            this.subValues = new float[0];
        }

        this.axisXLabels = axisXLabels;

        this.axisXName = axisXName;
        this.axisYName = axisYName;
    }

}
