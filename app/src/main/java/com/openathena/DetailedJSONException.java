package com.openathena;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailedJSONException extends JSONException {
    private final JSONObject offendingObject;

    public DetailedJSONException(String message, JSONObject offendingObject) {
        super(message);
        this.offendingObject = offendingObject;
    }

    public JSONObject getOffendingObject() {
        return offendingObject;
    }

    // obtain the makeModel field (if present) of the drone model which caused the exception
    public String getOffendingObjectMakeModel() {
        if (offendingObject == null) {
            return null;
        }
        try {
            String objName = offendingObject.getString("makeModel");
            if (objName.isBlank()) {
                return null;
            } else {
                return objName;
            }
        } catch (JSONException e) {
            return null;
        }
    }
}
