package de.beuth.bva.viciberlin.util;

/**
 * Created by betty on 25/11/15.
 */
public class Constants {

    // Intents
    public static final String ZIP_INTENT = "plzIntent";
    public static final String ZIP_EXTRA = "plzExtra";
    public static final String ZIP_NAME_EXTRA = "plzNameExtra";
    public static final String PREOPEN_EXTRA = "preopen";
    public static final String ZIPOFUSER_EXTRA = "zipOfUser";

    // Charts
    public static final String AGE_CHART = "ageChart";
    public static final String AGE_HISTORY_CHART = "ageHistoryChart";
    public static final String GENDER_CHART = "genderChart";
    public static final String LOCATION_CHART = "locationChart";
    public static final String DURATION_CHART = "durationChart";
    public static final String FOREIGNERS_CHART = "foreignersChart";
    public static final String FOREIGNERS_HISTORY_CHART = "foreignersHistoryChart";
    public static final String MOST_EQUAL_CHART = "mostEqualChart";
    public static final String LESS_EQUAL_CHART = "lessEqualChart";

    // Twitter Keys
    public static final String TWITTER_KEY = "NLkaOeflUB0j8bogUDwmlt5e4";
    public static final String TWITTER_SECRET = "SSeiYYNhy4cmX4RyflA0dOruugnUDVu4sP1jureS1xMW100teT";

    // Shared Preferences Keys
    public static final String SHARED_PREFS_VICI = "SharedPreferecesVici";
    public static final String TWITTER_AUTH_TOKEN = "TwitterAuthToken";
    public static final String TWITTER_AUTH_SECRET = "TwitterAuthSecret";
    public static final String TWITTER_USERNAME = "TwitterUsername";
    public static final String TWITTER_USERID = "TwitterUserid";

    // Rating API
    public static final String RATING_ENDPOINT = "http://vici-berlin.herokuapp.com/rating/";

    // CSV Files
    public static final String AGE_FILE = "age.csv";
    public static final String AGE_HISTORY_FILE = "age_2001.csv";
    public static final String AREA_FILE = "area.csv";
    public static final String FOREIGNERS_FILE = "foreigners.csv";
    public static final String FOREIGNERS_HISTORY_FILE = "foreigners_2001.csv";
    public static final String GENDER_FILE = "gender.csv";
    public static final String DURATION_FILE = "duration.csv";
    public static final String LOCATION_FILE = "location.csv";
    public static final String NAMES_FILE = "zipcode_names.csv";

    public static final String AVERAGE_LINE = "average";
    public static final String ZIPCODE_LINE = "zipcode";

    // Arrow states
    public static final String ARROW_UP = "arrow_up";

    // Map Values
    public static final double BERLIN_LAT = 52.5167;
    public static final double BERLIN_LNG = 13.3833;

    public static final double BERLIN_NORTH = 52.673235;
    public static final double BERLIN_EAST = 13.762254;
    public static final double BERLIN_SOUTH = 52.338880;
    public static final double BERLIN_WEST = 13.089341;

}
