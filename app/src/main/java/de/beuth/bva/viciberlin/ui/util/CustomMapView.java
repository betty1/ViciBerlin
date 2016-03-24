package de.beuth.bva.viciberlin.ui.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * Created by betty on 24/11/15.
 */
public class CustomMapView extends MapView {

    private static final String TAG = CustomMapView.class.getSimpleName();
    double longitude;
    double latitude;

    LocationPressListener pressListener;

    public CustomMapView(final Context context, final AttributeSet attrs) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    public CustomMapView(final Context context) {
        super(context, new DefaultResourceProxyImpl(context));
    }

    public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    @TargetApi(21)
    public CustomMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, new DefaultResourceProxyImpl(context), null, null, attrs);
    }

    public void setPressListener(LocationPressListener listener) {
        this.pressListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        int actionType = e.getAction();
        switch (actionType) {

            // Catch action down events and get latitude and longitude from projection
            case MotionEvent.ACTION_DOWN:
                Projection projection = getProjection();
                IGeoPoint loc = projection.fromPixels((int) e.getX(), (int) e.getY());
                latitude = (double) loc.getLatitudeE6() / 1000000;
                longitude = (double) loc.getLongitudeE6() / 1000000;
                break;
        }

        // forward event to GestureDetector
        gestureDetector.onTouchEvent(e);
        return super.dispatchTouchEvent(e);
    }

    final GestureDetector gestureDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    if (pressListener != null) {
                        pressListener.onLocationPress(latitude, longitude);
                    }
                }
            });

    public interface LocationPressListener {
        void onLocationPress(double lat, double lng);
    }

}

