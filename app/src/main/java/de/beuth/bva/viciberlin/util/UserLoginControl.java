package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by betty on 05/01/16.
 */
public class UserLoginControl {

    public static void saveUserDataToSharedPrefs(Context context, String token, String secret, String userId, String userName){
        // Save to Shared Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREFS_VICI, context.MODE_PRIVATE).edit();
        editor.putString(Constants.TWITTER_AUTH_TOKEN, token);
        editor.putString(Constants.TWITTER_AUTH_SECRET, secret);
        editor.putString(Constants.TWITTER_USERID, String.valueOf(userId));
        editor.putString(Constants.TWITTER_USERNAME, userName);
        editor.apply();
    }

    public static void deleteUserDataFromSharedPrefs(Context context){
        // Set all Twitter Shared Preference Values to null
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREFS_VICI, context.MODE_PRIVATE).edit();
        editor.putString(Constants.TWITTER_AUTH_TOKEN, null);
        editor.putString(Constants.TWITTER_AUTH_SECRET, null);
        editor.putString(Constants.TWITTER_USERID, null);
        editor.putString(Constants.TWITTER_USERNAME, null);
        editor.apply();
    }

    public static String getUserName(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS_VICI, context.MODE_PRIVATE);
        String twitterUserName = prefs.getString(Constants.TWITTER_USERNAME, null);
        return twitterUserName;
    }

    public static String getUserId(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS_VICI, context.MODE_PRIVATE);
        String twitterUserId = prefs.getString(Constants.TWITTER_USERID, null);
        return twitterUserId;
    }

    public static boolean isLoggedIn(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS_VICI, context.MODE_PRIVATE);
        String username = prefs.getString(Constants.TWITTER_USERNAME, null);
        String userid = prefs.getString(Constants.TWITTER_USERID, null);
        String token = prefs.getString(Constants.TWITTER_AUTH_TOKEN, null);
        String secret = prefs.getString(Constants.TWITTER_AUTH_SECRET, null);
        return (username != null && userid != null && token != null && secret != null);
    }

}
