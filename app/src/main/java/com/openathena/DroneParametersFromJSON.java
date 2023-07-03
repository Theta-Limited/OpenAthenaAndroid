package com.openathena;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DroneParametersFromJSON implements DroneParameterProvider{
    private static final String TAG = "DroneParametersFromJSON";

    protected static JSONArray droneArray;
    protected static Context myContext;

    DroneParametersFromJSON(Context context) {
        super();
        myContext = context;
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            droneArray = obj.getJSONArray("droneCCDParams");
        } catch (JSONException jse) {

        }
    }

    private static String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = myContext.getAssets().open("DroneModels/droneModels.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "loadJSONFromAsset: ", ex);
            return null;
        }
        return json;
    }

    @Override
    /**
     * Get all JSONObject(s) that match a given make and model string
     *
     * <p>
     *     Many drone models have an EXIF make/model name collision between their main color camera and their secondary thermal camera, even though each has its entirely own intrinsics. Whoever calls this function will need to match based on pixel width to find the correct parameters it actually needs
     * </p>
     */
    public JSONArray getMatchingDrones(String make, String model) {
        String makeModel = make.toLowerCase() + model.toUpperCase();
        JSONArray matchingDrones = new JSONArray();
        try {
            for (int i = 0; i < droneArray.length(); i++) {
                JSONObject droneObject = droneArray.getJSONObject(i);
                if (droneObject.getString("makeModel").equals(makeModel)) {
                    matchingDrones.put(droneObject);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "getDroneParameters: ", e);
        }

        return matchingDrones;
    }
}
