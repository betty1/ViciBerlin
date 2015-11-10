package de.beuth.bva.viciberlin.rest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by betty on 06/11/15.
 */
public class RestCall {

    static RestCallback callback;

    public static void startAPICall(String url, String callId, RestCallback callback){
        RestCall.callback = callback;

        new CallAPI().execute(url, callId);
    }

    private static class CallAPI extends AsyncTask<String, String, String> {

        String callId;

        @Override
        protected String doInBackground(String... params) {

            String urlString=params[0]; // URL to call
            callId=params[1]; // call ID

            InputStream in;
            String parsedResult = null;

            // HTTP Get
            try {

                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                in = new BufferedInputStream(urlConnection.getInputStream());
                parsedResult = convertInputStreamToString(in);


            } catch (Exception e ) {
                return e.getMessage();
            }

            return parsedResult;
        }

        protected void onPostExecute(String result) {
            Log.d("TAG", "callID: " + callId + ", result: " + result);
            callback.receiveResponse(result, callId);
        }



    } // end CallAPI

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public interface RestCallback {
        void receiveResponse(String result, String callId);
    }

}
