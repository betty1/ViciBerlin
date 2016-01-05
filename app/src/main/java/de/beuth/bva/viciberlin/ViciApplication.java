package de.beuth.bva.viciberlin;

import android.app.Application;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import de.beuth.bva.viciberlin.util.Constants;
import io.fabric.sdk.android.Fabric;

/**
 * Created by betty on 07/12/15.
 */
public class ViciApplication extends Application {

        private static ViciApplication singleton;

        public ViciApplication getInstance(){
            return singleton;
        }
        @Override
        public void onCreate() {
            super.onCreate();
            singleton = this;

            TwitterAuthConfig authConfig =  new TwitterAuthConfig(Constants.TWITTER_KEY, Constants.TWITTER_SECRET);
            Fabric.with(this, new TwitterCore(authConfig));
        }
}
