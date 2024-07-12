package com.openathena;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DroneParametersFromJSON {
    private static final String TAG = "DroneParametersFromJSON";

    protected static String lastUpdatedField;
    protected static JSONArray droneArray;
    protected Context myContext;

    DroneParametersFromJSON(Context context) {
        super();
        myContext = context;
//        loadJSONFromAsset();
    }

    // Load the droneModels.json bundled with the App upon release
    public void loadJSONFromAsset() {
        // reset the array of drone model objects
        droneArray = null;
        String jsonStr;
        try {
                                       // Assets directory is codebase path ./app/main/assets
                                       // Load the droneModels.json bundled with the App upon release
            InputStream is = myContext.getAssets().open("DroneModels/droneModels.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "loadJSONFromAsset: ", ex);
            return;
        }
        try {
            JSONObject obj = new JSONObject(jsonStr);
            lastUpdatedField = obj.getString("lastUpdate");
            droneArray = obj.getJSONArray("droneCCDParams");
        } catch (JSONException jse) {
            Log.e(TAG, "ERROR: App Default droneModels.json was invalid! Loading operation failed!");
            jse.printStackTrace();
        }
    }

    // Load a user's custom selected droneModels.json file
    public void loadJSONFromUri(Uri uri) throws IOException, DetailedJSONException{
        if (uri == null) throw new IOException("tried to load JSON from a null Uri!");
        droneArray = null;
        InputStream inputStream = myContext.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();

        // Update the static droneArray with new data
        JSONArray tempDroneArray;
        try {
            JSONObject obj = new JSONObject(stringBuilder.toString());
            lastUpdatedField = obj.getString("lastUpdate");
            tempDroneArray = obj.getJSONArray("droneCCDParams");
        } catch (JSONException e) {
            throw new DetailedJSONException(e.getMessage(), null);
        }

        // will throw and exit early if new JSON is invalid
        droneArrayIntegrityCheck(tempDroneArray);

        // if we've gotten here, the JSON is good
        droneArray = tempDroneArray;
    }

    public boolean isDroneArrayValid() {
        if (droneArray == null || droneArray.length() < 1) {
            return false;
        }

        try {
            droneArrayIntegrityCheck();
        } catch (DetailedJSONException e) {
            return false;
        }

        // If we've gotten here without catching a JSONException, the file is valid
        return true;
    }

    public void droneArrayIntegrityCheck() throws DetailedJSONException{
        droneArrayIntegrityCheck(droneArray);
    }

    public void droneArrayIntegrityCheck(JSONArray arr) throws DetailedJSONException {
        if (arr == null) {
            throw new DetailedJSONException(myContext.getString(R.string.error_dronemodels_was_null), null);
        }
        JSONObject droneObject = null;
        try {
            for (int i = 0; i < arr.length(); i++) {
                droneObject = arr.getJSONObject(i);
                // bunch of field checks, will throw a JSONException if any required field is missing
                droneObject.get("makeModel");
                droneObject.get("isThermal");
                droneObject.get("ccdWidthMMPerPixel");
                droneObject.get("ccdHeightMMPerPixel");
                droneObject.getDouble("widthPixels");
                droneObject.getDouble("heightPixels");
                droneObject.get("lensType");
                boolean isLensTypePerspective = droneObject.getString("lensType").equals("perspective");
                if (isLensTypePerspective) {
                    droneObject.getDouble("radialR1");
                    droneObject.getDouble("radialR2");
                    droneObject.getDouble("radialR3");
                    droneObject.getDouble("tangentialT1");
                    droneObject.getDouble("tangentialT2");
                } else {
                    droneObject.getDouble("poly0");
                    droneObject.getDouble("poly1");
                    droneObject.getDouble("poly2");
                    droneObject.getDouble("poly3");
                    droneObject.getDouble("poly4");
                    droneObject.getDouble("c");
                    droneObject.getDouble("d");
                    droneObject.getDouble("e");
                    droneObject.getDouble("f");
                }
            }
        } catch (JSONException e) {
            throw new DetailedJSONException(e.getMessage(), droneObject);
        }
    }

    public long getDroneArrayLength() {
        if (!isDroneArrayValid()) {
            return 0;
        } else {
            return droneArray.length();
        }
    }

    public String getLastUpdatedField() {
        if (lastUpdatedField == null) {
            return "";
        } else {
            return lastUpdatedField;
        }
    }

    /**
     * Get all JSONObject(s) that match a given make and model string
     *
     * <p>
     *     Many drone models have an EXIF make/model name collision between their main color camera and their secondary thermal camera, even though each has its entirely own intrinsics. Whoever calls this function will need to match based on pixel width to find the correct parameters it actually needs
     * </p>
     */
    public JSONArray getMatchingDrones(String make, String model) {
        if (droneArray == null || droneArray.length() < 1) {
            Log.e(TAG,"ERROR: attempted to getMatchingDrones while droneArray was null, or empty");
            // return an empty array if droneArray is not valid
            return new JSONArray();
        }
        if (make == null || make.isBlank() || model == null || model.isBlank()) {
            throw new IllegalArgumentException("ERROR: attempted to find getMatchingDrones for empty or null String!");
        }

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
