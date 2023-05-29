package com.openathena;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class AthenaActivity extends AppCompatActivity {

    public static String TAG;
    MarkableImageView iView;

    public static AthenaApp athenaApp; // singleton for storing persistent state information between activities

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

    protected Uri imageUri = null;
    public boolean isImageLoaded;
    protected Uri demUri = null;
    protected boolean isDEMLoaded;

    public AthenaActivity() {
        super();
        isTargetCoordDisplayed = false;
        isImageLoaded = false;
        isDEMLoaded = false;
    }
//    // selected image pixel for use in ray offset calculation
//    // represents (u, v) in pixels from the top left corner of the image
//    protected int selection_x = -1;
//    protected int selection_y = -1;

//    protected int cx = -1; // x position of the principal point (centerpoint of image). Measured from upper-left corner
//    protected int cy = -1; // y position of the principal point (centerpoint of image). Measured from upper-left corner

    @SuppressLint("SetTextI18n")
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.wgs84_standard_lat_lon));
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_1m));
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_10m));
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.nato_mgrs_100m));
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.ck_42_lat_lon));
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
                        textViewTargetCoord.setText("\uD83C\uDFAF " + getString(R.string.ck_42_gauss_kruger_n_e));
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

    public static int get_selection_x() {
        return athenaApp.get_selection_x();
    }

    public static void set_selection_x(int x) {
        athenaApp.set_selection_x(x);
    }

    public static int get_selection_y() {
        return athenaApp.get_selection_y();
    }

    public static void set_selection_y(int y) {
        athenaApp.set_selection_y(y);
    }

    public int[] getImageDimensionsFromUri(Uri imageUri) {
        if (imageUri == null) {
            return new int[] {0,0};
        }
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(is);
            int width = exif.getAttributeInt( ExifInterface.TAG_IMAGE_WIDTH, -1);
            int height = exif.getAttributeInt( ExifInterface.TAG_IMAGE_LENGTH, -1);
            if (width < 0 || height < 0) {
                return null;
            } else {
//                cx = width / 2; // x coordinate of the principal point (center) of the image. Measured from Top-Left corner
//                cy = height / 2; // y coordinate of the principal point (center) of the image. Measured from Top-Left corner
                return new int[] {width, height};
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to obtain image dimensions from EXIF metadata!");
            ioe.printStackTrace();
            return null;
        }
    }

    public abstract void calculateImage(View view);
    public abstract void calculateImage(View view, boolean shouldISendCoT);

//    public int get_cx() {
//        return cx;
//    }
//
//    public int get_cy() {
//        return cy;
//    }

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
        int[] width_and_height = getImageDimensionsFromUri(imageUri); // also updates cx and cy to that of new image
        int width = width_and_height[0];
        int height = width_and_height[1];
        String aspectRatio = width + ":" + height;
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

    protected abstract void saveStateToSingleton();

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
        if (isImageLoaded && iView != null) {
            if (get_selection_x() != -1 && get_selection_y() != -1) {
                iView.restoreMarker(get_selection_x(), get_selection_y());
            } else{
                iView.mark(0.5d, 0.5d); // put marker on center of iView if no current selection
            }
        }
        if (!isTargetCoordDisplayed) {
            restorePrefOutputMode(); // reset the textViewTargetCoord display
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");
        //appendText("onPause\n");
        super.onPause();
        saveStateToSingleton();
    } // onPause()

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        // close logfile
        //appendText("onDestroy\n");
        // do whatever here
        super.onDestroy();

    } // onDestroy()

    public boolean outputModeIsSlavic() {
        return (outputMode == outputModes.CK42GaussKrüger || outputMode == outputModes.CK42Geodetic);
    }

    public boolean outputModeIsMGRS() {
        return (outputMode == outputModes.MGRS1m || outputMode == outputModes.MGRS10m || outputMode == outputModes.MGRS100m);
    }
}
