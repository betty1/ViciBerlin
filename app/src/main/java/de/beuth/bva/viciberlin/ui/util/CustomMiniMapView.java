package de.beuth.bva.viciberlin.ui.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * Created by betty on 24/11/15.
 */
public class CustomMiniMapView extends MapView {

    private static final String TAG = "CustomMiniMapView";

    public CustomMiniMapView(final Context context, final AttributeSet attrs) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    public CustomMiniMapView(final Context context) {
        super(context, new DefaultResourceProxyImpl(context));
    }

    public CustomMiniMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    @TargetApi(21)
    public CustomMiniMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

}

