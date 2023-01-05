package com.openathena;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public abstract class AthenaActivity extends AppCompatActivity {

    public static String TAG;

    public enum outputModes {
        WGS84,
        MGRS1m,
        MGRS10m,
        MGRS100m,
        CK42Geodetic,
        CK42GaussKrüger
    }

    protected static PrefsActivity.outputModes outputMode;
    static RadioGroup radioGroup;
    protected TextView textViewTargetCoord;
    protected boolean isTargetCoordDisplayed;

    public void setOutputMode(int mode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        switch(mode) {
            case 0:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = PrefsActivity.outputModes.WGS84; // standard lat, lon format
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonWGS84);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.wgs84_standard_lat_lon));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode set to WGS84");
                break;
            case 1:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = PrefsActivity.outputModes.MGRS1m; // NATO Military Grid Ref, 1m square area
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonMGRS1m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.nato_mgrs_1m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS1m");
                break;
            case 2:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = PrefsActivity.outputModes.MGRS10m; // NATO Military Grid Ref, 10m square area
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonMGRS10m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.nato_mgrs_10m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS10m");
                break;
            case 3:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode= PrefsActivity.outputModes.MGRS100m; // NATO Military Grid Ref, 100m square area
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonMGRS100m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.nato_mgrs_100m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS100m");
                break;
            case 4:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = PrefsActivity.outputModes.CK42Geodetic; // An alternative geodetic system using the Krasovsky 1940 ellipsoid. Commonly used in former Warsaw pact countries
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonCK42Geodetic);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.ck_42_lat_lon));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to CK42Geodetic");
                break;
            case 5:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = PrefsActivity.outputModes.CK42GaussKrüger; // An alternative geodetic system using the Krasovsky 1940 ellipsoid. A longitudinal ZONE (in 6° increments, possible values 1-60 inclusive), Northing defined by X value, and Easting defined by Y value describe an exact position on Earth
                if (radioGroup != null && radioGroup.getVisibility() == View.VISIBLE) {
                    radioGroup.check(R.id.radioButtonCK42GaussKrüger);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText(getString(R.string.ck_42_gauss_kruger_n_e));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to CK42 GaussKrüger");
                break;
            default:
                Log.e(TAG, "ERROR: unrecognized value for output mode: " + mode + ". reverting to WGS84...");
                setOutputMode(0);
                break;
        }
    }

    // Overloaded
    public void setOutputMode(outputModes aMode) {
        if (aMode == null) {
            setOutputMode(-1); // should never happen
        } else {
            setOutputMode(aMode.ordinal()); // overloaded method call
        }
    }

    public void restorePrefOutputMode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences != null) {
            int outputModeFromPref = sharedPreferences.getInt("outputMode", 0);
            setOutputMode(outputModeFromPref);
        }
    }

    public boolean outputModeIsSlavic() {
        return (outputMode == outputModes.CK42GaussKrüger || outputMode == outputModes.CK42Geodetic);
    }

    public boolean outputModeIsMGRS() {
        return (outputMode == outputModes.MGRS1m || outputMode == outputModes.MGRS10m || outputMode == outputModes.MGRS100m);
    }
}
