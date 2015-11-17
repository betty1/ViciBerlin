package de.beuth.bva.viciberlin.rest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


import java.text.ParseException;

/**
 * Code sample for accessing the Yelp API V2.
 *
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 *
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 *
 */
public class OAuthYelpCall {

    final static String TAG = "OAuthYelpCall";

    private static final String API_HOST = "api.yelp.com";
    private static final int SEARCH_LIMIT = 1;
    private static final String SEARCH_PATH = "/v2/search";

    private static final String LOCATION_STRING = "berlin+de";

    private static final String CONSUMER_KEY = "DEnMAeUtRuTCVMX_BPGKjA";
    private static final String CONSUMER_SECRET = "BRLmIpG-zxnjMK10QqYVc8CXqR0";
    private static final String TOKEN = "94P6YIGTwQCi0yePMfTuo7s3VcO0jFLf";
    private static final String TOKEN_SECRET = "mQ0ySt7ura5whVF8VEQd7Uo6eKM";

    static OAuthService service;
    static Token accessToken;

    static OAuthYelpCallback callback;


    public static void startAPICall(String term, String plz, String callId, OAuthYelpCallback callback){
        OAuthYelpCall.callback = callback;

        OAuthYelpCall.service = new ServiceBuilder().provider(TwoStepYelpOAuth.class).apiKey(CONSUMER_KEY)
                .apiSecret(CONSUMER_SECRET).build();
        OAuthYelpCall.accessToken = new Token(TOKEN, TOKEN_SECRET);

        new AsyncYelpCall().execute(term, plz, callId);
    }

    private static class AsyncYelpCall extends AsyncTask<String, Void, String> {

        String callId;

        @Override
        protected String doInBackground(String... params) {
            String result = null;

            if (params.length > 2) {
                String term = params[0]; // term to search for
                String plz = params[1]; // plz
                callId = params[2]; // call ID
                try {
                    result = searchForBusinessesByLocation(term, plz);
                } catch(Exception e){
                    Log.d(TAG, "Exception while doInBackground.");
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            callback.receiveResponse(result, callId);
        }
    }

        /**
     * Creates and sends a request to the Search API by term and location.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term <tt>String</tt> of the search term to be queried
     * @param plz <tt>String</tt> of the plz
     * @return <tt>String</tt> JSON Response
     */
    public static String searchForBusinessesByLocation(String term, String plz) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", plz + "+" + LOCATION_STRING);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        request.addQuerystringParameter("radius_filter", String.valueOf(700));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private static OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private static String sendRequestAndGetResponse(OAuthRequest request) {
        System.out.println("Querying " + request.getCompleteUrl() + " ...");
        OAuthYelpCall.service.signRequest(OAuthYelpCall.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }


    public interface OAuthYelpCallback {
        void receiveResponse(String result, String callId);
    }

}