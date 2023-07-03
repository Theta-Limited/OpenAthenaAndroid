package com.openathena;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface DroneParameterProvider {
    public abstract JSONArray getMatchingDrones(String make, String model);
}
