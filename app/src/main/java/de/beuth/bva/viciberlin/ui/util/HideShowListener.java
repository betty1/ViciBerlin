package de.beuth.bva.viciberlin.ui.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

import de.beuth.bva.viciberlin.R;

/**
 * Created by betty on 10/11/15.
 */
public class HideShowListener implements View.OnClickListener {

    HashMap<Integer, Integer[]> viewMap = new HashMap<>();
    Activity context;

    public HideShowListener(Activity context){
        super();
        this.context = context;
        viewMap.put(R.id.age_header, new Integer[]{R.id.age_linearlayout, R.id.age_arrow});
        viewMap.put(R.id.age_equal_header, new Integer[]{R.id.age_equal_linearlayout, R.id.age_equal_arrow});
        viewMap.put(R.id.gender_header, new Integer[]{R.id.gender_linearlayout, R.id.gender_arrow});
        viewMap.put(R.id.location_header, new Integer[]{R.id.location_linearlayout, R.id.location_arrow});
        viewMap.put(R.id.location_equal_header, new Integer[]{R.id.location_equal_linearlayout, R.id.location_equal_arrow});
        viewMap.put(R.id.duration_header, new Integer[]{R.id.duration_linearlayout, R.id.duration_arrow});
        viewMap.put(R.id.duration_equal_header, new Integer[]{R.id.duration_equal_linearlayout, R.id.duration_equal_arrow});
    }

    @Override
    public void onClick(View v) {

        View childView = context.findViewById(viewMap.get(v.getId())[0]);
        ImageView arrowView = (ImageView) context.findViewById(viewMap.get(v.getId())[1]);

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
