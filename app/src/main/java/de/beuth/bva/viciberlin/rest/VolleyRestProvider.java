package de.beuth.bva.viciberlin.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by betty on 06/11/15.
 */
public class VolleyRestProvider {

    RestCallback callback;
    Context context;

    RequestQueue requestQueue;

    public VolleyRestProvider(Context context, RestCallback callback){
        this.callback = callback;
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
    }

    public void makeGETJSONRequest(final String url, final String callId){

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        callback.receiveResponse(response, callId);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonRequest);
    }

    public void makeGETStringRequest(final String url, final String callId) {

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        callback.receiveResponse(response, callId);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

            }
        });

        // Add the request to the queue
        requestQueue.add(stringRequest);

    }

    public void makePOSTRequest(final String url, final String callId, final Map<String, String> params){
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        callback.receiveResponse(response, callId);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // the POST parameters
                return params;
            }
        };
        requestQueue.add(postRequest);
    }


}
