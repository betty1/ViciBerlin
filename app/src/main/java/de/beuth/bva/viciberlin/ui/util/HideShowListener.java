package de.beuth.bva.viciberlin.ui.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashMap;

import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.util.Constants;

/**
 * Created by betty on 10/11/15.
 */
public class HideShowListener implements View.OnClickListener {

    HashMap<Integer, Integer[]> viewMap = new HashMap<>();
    Activity context;

    public HideShowListener(Activity context){
        super();
        this.context = context;
        viewMap.put(R.id.map_header, new Integer[]{R.id.map_relativelayout, R.id.map_arrow});
        viewMap.put(R.id.demography_header, new Integer[]{R.id.age_header, R.id.gender_header, R.id.location_header, R.id.duration_header, R.id.foreigners_header, R.id.demography_arrow});
        viewMap.put(R.id.age_header, new Integer[]{R.id.age_linearlayout, R.id.age_arrow});
        viewMap.put(R.id.age_equal_header, new Integer[]{R.id.age_equal_linearlayout, R.id.age_equal_arrow});
        viewMap.put(R.id.gender_header, new Integer[]{R.id.gender_linearlayout, R.id.gender_arrow});
        viewMap.put(R.id.location_header, new Integer[]{R.id.location_linearlayout, R.id.location_arrow});
        viewMap.put(R.id.location_equal_header, new Integer[]{R.id.location_equal_linearlayout, R.id.location_equal_arrow});
        viewMap.put(R.id.duration_header, new Integer[]{R.id.duration_linearlayout, R.id.duration_arrow});
        viewMap.put(R.id.duration_equal_header, new Integer[]{R.id.duration_equal_linearlayout, R.id.duration_equal_arrow});
        viewMap.put(R.id.foreigners_header, new Integer[]{R.id.foreigners_linearlayout, R.id.foreigners_arrow});
        viewMap.put(R.id.foreigners_equal_header, new Integer[]{R.id.foreigners_equal_linearlayout, R.id.foreigners_equal_arrow});
        viewMap.put(R.id.rating_header, new Integer[]{R.id.rating_linearlayout, R.id.rating_arrow});
        viewMap.put(R.id.yelp_header, new Integer[]{R.id.yelp_linearlayout, R.id.yelp_arrow});
        viewMap.put(R.id.twitter_header, new Integer[]{R.id.twitter_flowlayout, R.id.twitter_arrow});

    }

    private void hideView(int childId){
        try {
            // Look if view is arrow imageview
            ImageView arrowView = (ImageView) context.findViewById(childId);
            String tag = (String) arrowView.getTag();

            if(tag.contains("white")){

                arrowView.setTag("down_white");
                arrowView.setImageResource(R.drawable.android_arrow_white);

            } else {

                arrowView.setTag("down");
                arrowView.setImageResource(R.drawable.android_arrow);

            }

        } catch(Exception e){
            // If not an image view, hide the view

            ViewGroup childView = (ViewGroup) context.findViewById(childId);

            if(childView != null) {

                // Look if view itself can hide/show other views
                if(viewMap.get(childId) != null){

                    // Hide subviews
                    for(int subChildId: viewMap.get(childId)) {
                        hideView(subChildId);
                    }

                }

                childView.setVisibility(View.GONE);
            }
        }
    }

    private void showView(int childId){

        try {
            // Look if view is arrow imageview
            ImageView arrowView = (ImageView) context.findViewById(childId);
            String tag = (String) arrowView.getTag();

            if(tag.contains("white")){

                arrowView.setTag("up_white");
                arrowView.setImageResource(R.drawable.android_arrowup_white);

            } else {

                arrowView.setTag("up");
                arrowView.setImageResource(R.drawable.android_arrowup);

            }

        } catch(Exception e){
            // If not an image view, show the view

            ViewGroup childView = (ViewGroup) context.findViewById(childId);

            if(childView != null) {
                childView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {

        View firstChildView = context.findViewById(viewMap.get(v.getId())[0]);

        // Check if child view exists and if it's shown or hidden
        if(firstChildView != null && firstChildView.getVisibility() == View.VISIBLE){

            // Hide child View
            for(int childId: viewMap.get(v.getId())){
                hideView(childId);
            }

        } else if(firstChildView != null && firstChildView.getVisibility() == View.GONE) {

            // Show child view
            for(int childId: viewMap.get(v.getId())){
                showView(childId);
            }

        }
    }
}
