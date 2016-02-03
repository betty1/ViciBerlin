package de.beuth.bva.viciberlin;

import android.app.Activity;
import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

import de.beuth.bva.viciberlin.ui.MainActivity;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity activity;

    public ActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp(){
        this.activity = getActivity();
    }

//    public void testMapViewExists(){
//        assertNotNull(activity.findViewById(R.id.map));
//    }
//
//    public void testTitle(){
//        assertEquals("ViciBerlin", activity.getTitle());
//    }
}