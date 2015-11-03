package de.beuth.bva.viciberlin.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.model.ChartAttributes;
import de.beuth.bva.viciberlin.util.CSVParser;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class PLZActivity extends AppCompatActivity {

    @Bind(R.id.age_header) TextView ageHeader;
    @Bind(R.id.age_chart) ColumnChartView ageChart;
    @Bind(R.id.gender_header) TextView genderHeader;
    @Bind(R.id.gender_chart) ColumnChartView genderChart;
    @Bind(R.id.location_header) TextView locationHeader;
    @Bind(R.id.location_chart) ColumnChartView locationChart;

    final String TAG = "PLZActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plz);
        ButterKnife.bind(this);

        fillCharts();

        // Set UI Listener
        HideShowListener hideShowListener = new HideShowListener();
        ageHeader.setOnClickListener(hideShowListener);
        genderHeader.setOnClickListener(hideShowListener);
        locationHeader.setOnClickListener(hideShowListener);

    }

    private void fillCharts(){
        float[] ageValues = CSVParser.getFloatValuesForPLZ(this, "age.csv", "13353");
        String[] ageLabels = new String[]{"bis 12", "12-18", "18-35", "35-65", "ab 65"};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, null, ageLabels, "Lebensjahre", "Prozent");
        dataToChart(ageAttrs, ageChart);

        float[] genderValues = CSVParser.getFloatValuesForPLZ(this, "gender.csv", "13353");
        Log.d(TAG, "gender Values: " + genderValues[0]);
        String[] genderLabels = new String[]{"Männer", "Frauen"};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, null, genderLabels, "", "Prozent");
        dataToChart(genderAttrs, genderChart);

        float[] locationValues = CSVParser.getFloatValuesForPLZ(this, "wohnlage.csv", "13353", 1);
        String[] locationLabels = new String[]{"Einfach", "Mittel", "Hoch"};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, null, locationLabels, "", "Prozent");
        dataToChart(locationAttrs, locationChart);
    }

    private void dataToChart(ChartAttributes attrs, ColumnChartView chart) {

        float[] values = attrs.values;
        float[] subValues = attrs.subValues;
        String[] axisXLabels = attrs.axisXLabels;

        if(axisXLabels.length != values.length){
            Log.d(TAG, "Wrong number of values/axisXLabels");
            return;
        }

        int numColumns = values.length;
        boolean hasSubColumn = false;

        // Check for subColumnValues
        if(subValues.length == values.length){
            hasSubColumn = true;
        }

        List<SubcolumnValue> tempValues;
        List<AxisValue> xValues = new ArrayList<AxisValue>();;
        List<Column> columns = new ArrayList<Column>();
        int columnColor = 0;

        for (int i = 0; i < numColumns; ++i) {

            int newColor = ChartUtils.pickColor();
            while(newColor == columnColor){
                newColor = ChartUtils.pickColor();
            }
            columnColor = newColor;

            tempValues = new ArrayList<SubcolumnValue>();
            tempValues.add(new SubcolumnValue(values[i], columnColor));

            if(hasSubColumn){
                tempValues.add(new SubcolumnValue(subValues[i], columnColor));
            }

            Column column = new Column(tempValues);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);

             xValues.add(new AxisValue(i).setLabel(axisXLabels[i]));
        }

        ColumnChartData data = new ColumnChartData(columns);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName(attrs.axisXName);
        axisY.setName(attrs.axisYName);

        axisX.setValues(xValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        chart.setColumnChartData(data);
    }

    class HideShowListener implements View.OnClickListener {

        HashMap<Integer, Integer> viewMap = new HashMap<>();

        HideShowListener(){
            super();
            viewMap.put(R.id.age_header, R.id.age_chart);
            viewMap.put(R.id.gender_header, R.id.gender_chart);
            viewMap.put(R.id.location_header, R.id.location_chart);
        }

        @Override
        public void onClick(View v) {

            View childView = findViewById(viewMap.get(v.getId()));
            if(childView != null){

                if(childView.getVisibility() == View.GONE){
                    childView.setVisibility(View.VISIBLE);
                } else {
                    childView.setVisibility(View.GONE);
                }

            } else {
                Log.d(TAG, "HideShowListener: No child view found.");
            }

        }
    }
}
