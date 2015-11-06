package de.beuth.bva.viciberlin.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
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
    @Bind(R.id.duration_header) TextView durationHeader;
    @Bind(R.id.duration_chart) ColumnChartView durationChart;

    final int PURPLE = R.color.graph_purple;
    final int GREEN = R.color.graph_green;
    final int BLUE = R.color.graph_blue;
    final int DARKBLUE = R.color.graph_darkblue;

    final String TAG = "PLZActivity";

    String plz = "13353";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plz);
        ButterKnife.bind(this);

        updatePlz();

        // Set UI Listener
        HideShowListener hideShowListener = new HideShowListener();
        ageHeader.setOnClickListener(hideShowListener);
        genderHeader.setOnClickListener(hideShowListener);
        locationHeader.setOnClickListener(hideShowListener);
        durationHeader.setOnClickListener(hideShowListener);

        // Search
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            plz = query;
            updatePlz();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private void updatePlz(){
        fillCharts();
        getSupportActionBar().setTitle(plz);
    }

    private void fillCharts(){
        float[] ageValues = CSVParser.getFloatValuesForPLZ(this, "age.csv", plz);
        String[] ageLabels = new String[]{"bis 12", "12-18", "18-35", "35-65", "ab 65"};
        ChartAttributes ageAttrs = new ChartAttributes(ageValues, null, ageLabels, "Lebensjahre", "Prozent");
        dataToChart(ageAttrs, ageChart, new int[]{GREEN, PURPLE, GREEN, PURPLE, GREEN});

        float[] genderValues = CSVParser.getFloatValuesForPLZ(this, "gender.csv", plz);
        String[] genderLabels = new String[]{"Männer", "Frauen"};
        ChartAttributes genderAttrs = new ChartAttributes(genderValues, null, genderLabels, "", "Prozent");
        dataToChart(genderAttrs, genderChart, new int[]{DARKBLUE, BLUE});

        float[] locationValues = CSVParser.getFloatValuesForPLZ(this, "wohnlage.csv", plz, 1);
        String[] locationLabels = new String[]{"Einfach", "Mittel", "Gut"};
        ChartAttributes locationAttrs = new ChartAttributes(locationValues, null, locationLabels, "", "Prozent");
        dataToChart(locationAttrs, locationChart, null);

        float[] durationValues = CSVParser.getFloatValuesForPLZ(this, "wohndauer.csv", plz);
        String[] durationLabels = new String[]{"unter 5 Jahre", "5-10 Jahre", "über 10 Jahre"};
        ChartAttributes durationAttrs = new ChartAttributes(durationValues, null, durationLabels, "", "Prozent");
        dataToChart(durationAttrs, durationChart, null);
    }

    private void dataToChart(ChartAttributes attrs, ColumnChartView chart, int[] colors) {

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
        boolean hasColors = false;

        if(colors != null) {
            if (colors.length == numColumns) {
                hasColors = true;
                Log.d(TAG, "has colors");
            }
        }

        for (int i = 0; i < numColumns; ++i) {

            if(hasColors) {
                columnColor = ContextCompat.getColor(this, colors[i]);
                Log.d(TAG, "HAS COLORS - Color of column nr. " + i + ": " + columnColor);
            } else {
                int newColor = ChartUtils.pickColor();
                while(newColor == columnColor){
                    newColor = ChartUtils.pickColor();
                }
                columnColor = newColor;
                Log.d(TAG, "NO COLORS - Color of column nr. " + i + ": " + columnColor);
            }

            tempValues = new ArrayList<SubcolumnValue>();
            tempValues.add(new SubcolumnValue(values[i], columnColor));

            if(hasSubColumn){
                tempValues.add(new SubcolumnValue(subValues[i], columnColor));
            }

            Column column = new Column(tempValues);
            column.setHasLabels(true);
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

        HashMap<Integer, Integer[]> viewMap = new HashMap<>();

        HideShowListener(){
            super();
            viewMap.put(R.id.age_header, new Integer[]{R.id.age_chart, R.id.age_arrow});
            viewMap.put(R.id.gender_header, new Integer[]{R.id.gender_chart, R.id.gender_arrow});
            viewMap.put(R.id.location_header, new Integer[]{R.id.location_chart, R.id.location_arrow});
            viewMap.put(R.id.duration_header, new Integer[]{R.id.duration_chart, R.id.duration_arrow});

        }

        @Override
        public void onClick(View v) {

            View childView = findViewById(viewMap.get(v.getId())[0]);
            ImageView arrowView = (ImageView) findViewById(viewMap.get(v.getId())[1]);

            if(childView != null){

                if(childView.getVisibility() == View.GONE){
                    childView.setVisibility(View.VISIBLE);
                } else {
                    childView.setVisibility(View.GONE);
                }

            }
            if(arrowView != null){

                if(arrowView.getTag().equals("down")){
                    arrowView.setTag("up");
                    arrowView.setImageResource(R.drawable.android_arrow);
                } else {
                    arrowView.setTag("down");
                    arrowView.setImageResource(R.drawable.android_arrowup);
                }
            }

        }
    }
}
