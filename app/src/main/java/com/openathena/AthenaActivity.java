package com.openathena;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.exifinterface.media.ExifInterface;


import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Locale;

public abstract class AthenaActivity extends AppCompatActivity {

    public static String TAG;
    MarkableImageView iView;

    public static AthenaApp athenaApp; // singleton for storing persistent state information between activities

    public static int requestNo = 0;

    public static int dangerousMaritimeModeActivatedCount;

    public enum outputModes {
        WGS84,
        WGS84_DMS,
        USNG,  // USNG is functionally equivalent to MGRS
        UTM,
        MGRS1m,
        MGRS10m,
        MGRS100m,
        CK42Geodetic,
        CK42GaussKruger
    }



    public String getCurrentOutputModeName() {
        switch (outputMode) {
            case WGS84:
                return getString(R.string.wgs84_standard_lat_lon);
            case WGS84_DMS:
                return getString(R.string.wgs84_lat_lon_dms);
            case USNG:
                return getString(R.string.u_s_national_grid);
            case UTM:
                return getString(R.string.utm);
            case MGRS1m:
                return getString(R.string.nato_mgrs_1m);
            case MGRS10m:
                return getString(R.string.nato_mgrs_10m);
            case MGRS100m:
                return getString(R.string.nato_mgrs_100m);
            case CK42Geodetic:
                return getString(R.string.ck_42_lat_lon);
            case CK42GaussKruger:
                return getString(R.string.ck_42_gauss_kruger_n_e);
        }
        return ""; // this should never happen
    }

    public enum measurementUnits {
        METER,
        FOOT // US survey foot used in Aviation, 1200/3937 = 0.30480061 meters
    }

    public String getCurrentMeasurementUnitName() {
        switch (measurementUnit) {
            case METER:
                return getString(R.string.meter_label);
            case FOOT:
                return "ft.";
        }
        return ""; // this should never happen
    }

    protected static PrefsActivity.outputModes outputMode;
    protected static AthenaActivity.measurementUnits measurementUnit;
    static RadioGroup outputModeRadioGroup;
    static RadioGroup measurementUnitRadioGroup;

    static SeekBar compassCorrectionSeekBar;
    static TextView compassCorrectionValue;

    protected static boolean isMaritimeModeEnabled;

    protected double compassCorrectionOffset = 0.0d; // default value
    protected TextView textViewTargetCoord;
    protected boolean isTargetCoordDisplayed;

    protected Uri imageUri = null;
    public boolean isImageLoaded;
    protected Uri demUri = null;
    protected boolean isDEMLoaded;

    public Uri droneModelsJsonUri = null;


    // The most recent measured or extracted point of interest for finding a DEM with sufficient coverage
    protected static String lastPointOfInterest = "";

    public AthenaActivity() {
        super();
        isTargetCoordDisplayed = false;
        isImageLoaded = false;
        isDEMLoaded = false;
    }

    // Overloaded
    public void setOutputMode(outputModes aMode) {
        if (aMode == null) {
            setOutputMode(-1); // should never happen
        } else {
            setOutputMode(aMode.ordinal()); // overloaded method call
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_OpenAthenaAndroid);
        super.onCreate(savedInstanceState);
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat ctl =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        ctl.setAppearanceLightStatusBars(!isDarkMode);
    }

    @SuppressLint("SetTextI18n")
    public void setOutputMode(int mode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        switch(mode) {
            case 0:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.WGS84; // standard lat, lon format
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonWGS84);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.wgs84_standard_lat_lon));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode set to WGS84");
                break;
            case 1:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.WGS84_DMS; // standard lat, lon format
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonWGS84_DMS);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.wgs84_lat_lon_dms));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode set to WGS84 DMS");
                break;
            case 2:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply();
                outputMode = outputModes.USNG;
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonUSNG);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.u_s_national_grid));
                    }
                }
                Log.i(TAG, "Output mode set to USNG");
                break;
            case 3:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply();
                outputMode = outputModes.UTM;
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonUTM);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.utm));
                    }
                }
                Log.i(TAG, "Output mode set to UTM");
                break;
            case 4:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.MGRS1m; // NATO Military Grid Ref, 1m square area
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonMGRS1m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_1m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS1m");
                break;
            case 5:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.MGRS10m; // NATO Military Grid Ref, 10m square area
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonMGRS10m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_10m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS10m");
                break;
            case 6:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode= outputModes.MGRS100m; // NATO Military Grid Ref, 100m square area
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonMGRS100m);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_100m));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to MGRS100m");
                break;
            case 7:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.CK42Geodetic; // An alternative geodetic system using the Krasovsky 1940 ellipsoid. Commonly used in former Warsaw pact countries
                // There is no reason anyone would ever use anything but Meter as the unit for CK42
                setMeasurementUnit(measurementUnits.METER);
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonCK42Geodetic);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.ck_42_lat_lon));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to CK42Geodetic");
                break;
            case 8:
                prefsEditor.putInt("outputMode", mode);
                prefsEditor.apply(); // make the outputMode change persistent
                outputMode = outputModes.CK42GaussKruger; // An alternative geodetic system using the Krasovsky 1940 ellipsoid. Northing defined by X value, and Easting defined by Y value describe an exact position on Earth
                // There is no reason anyone would ever use anything but Meter as the unit for CK42
                setMeasurementUnit(measurementUnits.METER);
                if (outputModeRadioGroup != null && outputModeRadioGroup.getVisibility() == View.VISIBLE) {
                    outputModeRadioGroup.check(R.id.radioButtonCK42GaussKruger);
                }
                if (textViewTargetCoord != null && textViewTargetCoord.getVisibility() == View.VISIBLE) {
                    if(!isTargetCoordDisplayed) {
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.ck_42_gauss_kruger_n_e));
                        isTargetCoordDisplayed = false;
                    }
                }
                Log.i(TAG, "Output mode changed to CK42 GaussKruger");
                break;
            default:
                Log.e(TAG, "ERROR: unrecognized value for output mode: " + mode + ". reverting to WGS84...");
                setOutputMode(0);
                break;
        }
    }



    public void setMeasurementUnit(measurementUnits aUnit) {
        if (aUnit == null) {
            setMeasurementUnit(-1); // should never happen
        }
        else {
            setMeasurementUnit(aUnit.ordinal());
        }
    }

    public void setMeasurementUnit(int unit) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        switch(unit) {
            case 0:
                prefsEditor.putInt("measurementUnit", unit);
                prefsEditor.apply(); // make the measurementUnit change persistent
                measurementUnit = measurementUnits.METER;
                if (measurementUnitRadioGroup != null && measurementUnitRadioGroup.getVisibility() == View.VISIBLE) {
                    measurementUnitRadioGroup.check(R.id.radioButtonMETER);
                }
                Log.i(TAG, "Measurement unit changed to METER");
                break;
            case 1:
                prefsEditor.putInt("measurementUnit", unit);
                prefsEditor.apply(); // make the measurementUnit change persistent
                measurementUnit = measurementUnits.FOOT;
                if (measurementUnitRadioGroup != null && measurementUnitRadioGroup.getVisibility() == View.VISIBLE) {
                    measurementUnitRadioGroup.check(R.id.radioButtonFOOT);
                }
                Log.i(TAG, "Measurement unit changed to us survey FOOT");
                break;
            default:
                Log.e(TAG, "ERROR: unrecognized value for measurement unit: " + unit + ". reverting to METER...");
                setMeasurementUnit(0);
                break;
        }
    }

    public double calculateCompassCorrectionOffset(int seekBarValue) {
        // Convert the seekBarValue (0-200) to a range of -1 to 1
        double mappedValue = (seekBarValue / 100.0) - 1;

        // Use a log function to gradually increase effect of SeekBar as further away from middle
        final double LOG_SCALE = 0.05;
        double logValue;
        if (mappedValue > 0) {
            logValue = Math.log(mappedValue * LOG_SCALE + 1);
        } else if (mappedValue < 0) {
            logValue = -Math.log(-mappedValue * LOG_SCALE + 1);
        } else {
            logValue = 0;
        }

        // Scale the result to fit within the range of [-15, 15]
        double offset = logValue * (15.0 / Math.log(1 + LOG_SCALE));
        return offset;
    }

    protected void setIsMaritimeModeEnabled(boolean bool) {
        isMaritimeModeEnabled = bool;
        // apply value to AthenaApp singleton for consumption by DEMParser
        AthenaApp.setIsMaritimeModeEnabled(bool);

        // save value to persistent settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("isMaritimeModeEnabled", isMaritimeModeEnabled);
        prefsEditor.apply();
    }

    protected void requestExternStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES}, requestNo);
            }
            requestNo++;
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(TAG, "Attempting to Obtain unobtained permission READ_EXTERNAL_STORAGE");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, requestNo);
                requestNo++;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AthenaActivity.this, getString(R.string.permissions_toast_success_msg), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AthenaActivity.this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

//    public static int get_selection_x() {
//        return athenaApp.get_selection_x();
//    }
//
//    public static void set_selection_x(int x) {
//        athenaApp.set_selection_x(x);
//    }
//
//    public static int get_selection_y() {
//        return athenaApp.get_selection_y();
//    }
//
//    public static void set_selection_y(int y) {
//        athenaApp.set_selection_y(y);
//    }
//
//    public static double get_proportion_selection_x() {
//        return athenaApp.get_proportion_selection_x();
//    }
//
//    public static void set_proportion_selection_x(double prop_x) {
//        athenaApp.set_proportion_selection_x(prop_x);
//    }
//
//    public static double get_proportion_selection_y() {
//        return athenaApp.get_proportion_selection_y();
//    }
//
//    public static void set_proportion_selection_y(double prop_y) {
//        athenaApp.set_proportion_selection_y(prop_y);
//    }

    public int[] getImageDimensionsFromUri(Uri imageUri) {
        Context context = this;
        if (imageUri == null) {
            return new int[]{0, 0};
        }
        ParcelFileDescriptor parcelFileDescriptor = null;
        int[] dimensions = null;

        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(imageUri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

                dimensions = new int[]{options.outWidth, options.outHeight};

                try {
                    ExifInterface exif = new ExifInterface(fileDescriptor);
                    int exifWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
                    int exifHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);

                    // If EXIF dimensions are inconsistent with actual dimensions, update the EXIF data.
                    if (exifWidth != options.outWidth || exifHeight != options.outHeight) {
                        exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(options.outWidth));
                        exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(options.outHeight));
                        exif.saveAttributes();
                    }
                } catch (IOException exifException) {
                    Log.e(TAG, "Failed to update EXIF data!", exifException);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to obtain image dimensions from image itself!", e);
        } finally {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close ParcelFileDescriptor", e);
                }
            }
        }
        return dimensions;
    }


    public abstract void calculateImage(View view);
    public abstract void calculateImage(View view, boolean shouldISendCoT);

//    public int get_
//    () {
//        return cx;
//    }
//
//    public int get_cy() {
//        return cy;
//    }

    protected String getDemApiKey() {
        String apiKey = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        apiKey = sharedPreferences.getString("OPENTOPOGRAPHY_API_KEY", "");
        if (apiKey.isEmpty()) {
            // Revert to the default bundled API key if user has not set one
            // NOTE: An empty (invalid) API key will be bundled with releases of this software on GitHub and F-Droid!
            // You will need to set your own in the local.properties file in the root of this project
            // or input your key into the app manually.
            // See README.md of this project for further instructions
            apiKey = BuildConfig.OPENTOPOGRAPHY_API_KEY;
        }
        return apiKey;
    }

    protected void resetDemApiKey() {
        String apiKey = BuildConfig.OPENTOPOGRAPHY_API_KEY;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("OPENTOPOGRAPHY_API_KEY", BuildConfig.OPENTOPOGRAPHY_API_KEY);
        prefsEditor.apply();
    }

    protected boolean putDemApiKey(String newApiKey) {
        // input validity checks
        if (newApiKey == null || newApiKey.trim().isEmpty()) return false;
        newApiKey = newApiKey.trim();
        if (newApiKey.length() != 32) return false;
        if (!StringUtils.isAlphanumeric(newApiKey)) return false;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("OPENTOPOGRAPHY_API_KEY", newApiKey);
        prefsEditor.apply();
        return true;
    }

    public void restorePrefs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences != null) {
            int outputModeFromPref = sharedPreferences.getInt("outputMode", 0);
            setOutputMode(outputModeFromPref);

            int measurementUnitFromPref = sharedPreferences.getInt("measurementUnit", 0);
            setMeasurementUnit(measurementUnitFromPref);

            int savedSeekBarValue = sharedPreferences.getInt("compassCorrectionSeekBarValue", 100);
            setCompassCorrectionSeekBar(savedSeekBarValue);

            // restore value from saved settings
            AthenaActivity.isMaritimeModeEnabled = sharedPreferences.getBoolean("isMaritimeModeEnabled", false);
            // apply value to AthenaApp singleton for consumption by DEMParser
            AthenaApp.isMaritimeModeEnabled = AthenaActivity.isMaritimeModeEnabled;
        }
    }

    public void setCompassCorrectionSeekBar(int value) {
        compassCorrectionOffset = calculateCompassCorrectionOffset(value);
        if (compassCorrectionSeekBar != null) {
            compassCorrectionSeekBar.setProgress(value);
        }
        updateCompassCorrection(value);
    }

    public void updateCompassCorrection(int value) {
        compassCorrectionOffset = calculateCompassCorrectionOffset(value);
        if (compassCorrectionSeekBar != null) {
            compassCorrectionValue.setText(getString(R.string.prefs_compass_offset_label) + " " + String.format(Locale.US, "%.2f°", compassCorrectionOffset));
        }
        if(athenaApp != null) {
            athenaApp.putDouble("userOffset", compassCorrectionOffset); // update the Singleton
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;

        int id = item.getItemId();

        if (id == R.id.action_calculate) {
            // jump to main activity
            // its already created
            intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about) {
            intent = new Intent(getApplicationContext(),AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_prefs) {
            intent = new Intent(getApplicationContext(), PrefsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_managedems) {
            Log.d(TAG,"AthenaActivity: going to launch manage dems");
            // jump to manage dems activity
            intent = new Intent(getApplicationContext(),ManageDemsActivity.class);
            Log.d(TAG,"AthenaActivity: created intent, going to start");
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_manage_drone_models_and_api_key) {
            Log.d(TAG, "AthenaActivity: going to launch manage drone models and api key");
            intent = new Intent(getApplicationContext(), ManageDroneModelsAndAPIKeyActivity.class);
            Log.d(TAG, "AthenaActivity: created intent, going to start");
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

//        if (id == R.id.action_log) {
//            intent = new Intent(getApplicationContext(),ActivityLog.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(intent);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    protected void constrainViewAspectRatio() {
        // Force the aspect ratio to be same as original image
        int[] width_and_height = getImageDimensionsFromUri(imageUri);
        float width = (float) width_and_height[0];
        float height = (float) width_and_height[1];
        String aspectRatio = "" + (width / height);
        Drawable drawable = iView.getDrawable();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) iView.getLayoutParams();
        layoutParams.dimensionRatio = aspectRatio;
        iView.setLayoutParams(layoutParams);
        iView.invalidate();
    }

    public boolean isCacheUri(Uri uri) {
        File cacheDir = getCacheDir();
        String cachePath = cacheDir.getAbsolutePath();
        String uriPath = uri.getPath();
        return uriPath.startsWith(cachePath);
    }

    protected String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        Log.d(TAG,"onSaveInstanceState started");
        super.onSaveInstanceState(saveInstanceState);
        //saveInstanceState.putBoolean("isMaritimeModeEnabled", isMaritimeModeEnabled);
        saveStateToSingleton();
    }

    protected abstract void saveStateToSingleton();

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
        if (isImageLoaded && iView != null) {
            if (AthenaApp.get_selection_x() != -1 && AthenaApp.get_selection_y() != -1) {
                iView.restoreMarker(AthenaApp.get_selection_x(), AthenaApp.get_selection_y());
            } else{
                iView.mark(0.5d, 0.5d); // put marker on center of iView if no current selection
            }
        }
        if (!isTargetCoordDisplayed) {
            restorePrefs(); // reset the textViewTargetCoord display
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");
        super.onPause();
        saveStateToSingleton();
    } // onPause()

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        super.onDestroy();

    } // onDestroy()

    public boolean outputModeIsSlavic() {
        return (outputMode == outputModes.CK42GaussKruger || outputMode == outputModes.CK42Geodetic);
    }

    public boolean outputModeIsMGRS() {
        return (outputMode == outputModes.MGRS1m || outputMode == outputModes.MGRS10m || outputMode == outputModes.USNG || outputMode == outputModes.MGRS100m);
    }

    public boolean isUnitFoot() {
        return (measurementUnit == measurementUnits.FOOT);
    }
}
