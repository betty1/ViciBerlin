package de.beuth.bva.viciberlin.rest;

import org.json.JSONObject;

/**
 * Created by betty on 05/01/16.
 */
public interface RestCallback {
    void receiveResponse(String result, String callId);
    void receiveResponse(JSONObject result, String callId);
}
