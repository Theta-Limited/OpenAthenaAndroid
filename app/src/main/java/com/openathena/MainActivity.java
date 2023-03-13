// MainActivity.java
// Bobby Krupczak, rdk@krupczak.org, Matthew Krupczak, mwk@krupzak.org, et. al

// main activity; launch everything from here

// we need to figure out how to go back and forth between activities
// via our menu w/o forcing destroy and create
// Do this by adding flag to newly created intent which tells
// android to use existing activity rather than create new on
// if possible; otherwise create new activity
// intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

package com.openathena;

// import veraPDF fork of Adobe XMP core Java v5.1.0
import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
import mil.nga.mgrs.grid.GridType;
import mil.nga.tiff.util.TiffException;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;

public class MainActivity extends AthenaActivity {
    public static String TAG = MainActivity.class.getSimpleName();
    public final static String PREFS_NAME = "openathena.preferences";
    public final static String LOG_NAME = "openathena.log";
    public static int requestNo = 0;
    public static int dangerousAutelAwarenessCount;

    TextView textView;
    MarkableImageView iView;

    ProgressBar progressBar;

    Button buttonSelectDEM;
    Button buttonSelectImage;
    Button buttonCalculate;

    protected String versionName;


    MetadataExtractor theMeta = null;
    GeoTIFFParser theParser = null;
    TargetGetter theTGetter = null;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        //appendText("Back from chooser\n");

                        if (uri == null)
                            return;

                        //appendText("Back from chooser\n");
                        Log.d(TAG,"back from chooser for image");
                        imageSelected(uri);
                    }
                });

    ActivityResultLauncher<String> mGetDEM = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    //appendText("Back from chooser\n");

                    if (uri == null)
                        return;

                    //appendText("Back from chooser\n");
                    Log.d(TAG,"back from chooser for DEM");
                    demSelected(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        radioGroup = null;

        progressBar = (ProgressBar)  findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        buttonSelectDEM = (Button) findViewById(R.id.selectDEMButton); // ⛰
        buttonSelectImage = (Button) findViewById(R.id.selectImageButton); // ð
        buttonCalculate = (Button) findViewById(R.id.calculateButton); // ð
        setButtonReady(buttonSelectDEM, true);
        setButtonReady(buttonSelectImage, false);
        setButtonReady(buttonCalculate, false);

        dangerousAutelAwarenessCount = 0;
        isImageLoaded = false;
        isDEMLoaded = false;

        theMeta = new MetadataExtractor(this);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textViewTargetCoord = (TextView)findViewById(R.id.textViewTargetCoord);
        textViewTargetCoord.setMovementMethod(LinkMovementMethod.getInstance());

        iView = (MarkableImageView) findViewById(R.id.imageView);

        // try to get our version out of app/build.gradle
        // versionName field
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            Log.d(TAG, "Got version " + versionName);
        }
        catch (Exception e) {
            versionName = "unknown";
        }

        // check for saved state

        // open logfile for logging?  No, only open when someone calls
        // append

        clearText();
//        appendLog("OpenAthena™ for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\n GPL-3.0, some rights reserved\n");

        if (savedInstanceState != null) {
            CharSequence textRestore = savedInstanceState.getCharSequence("textview");
            if (textRestore != null) {
                textView.setText(textRestore);
            }
            CharSequence textViewTargetCoordRestore = savedInstanceState.getCharSequence("textViewTargetCoord");
            if (textViewTargetCoordRestore != null) {
                textViewTargetCoord.setText(textViewTargetCoordRestore);
            }

            String storedUriString = savedInstanceState.getString("imageUri");
            if (storedUriString != null) {
                imageUri = Uri.parse(storedUriString);
                isImageLoaded = true;
                AssetFileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(imageUri , "r");
                } catch(FileNotFoundException e) {
                    imageUri = null;
                    isImageLoaded = false;
                }
                if (imageUri != null) {
                    long filesize = fileDescriptor.getLength();
                    if (filesize < 1024 * 1024 * 20) { // check if filesize below 20Mb
                        iView.setImageURI(imageUri);
                    }  else { // otherwise:
                        Toast.makeText(MainActivity.this, getString(R.string.image_is_too_large_error_msg), Toast.LENGTH_SHORT).show();
                        iView.setImageResource(R.drawable.athena); // put up placeholder icon
                    }
                }
            }

            String storedDEMUriString = savedInstanceState.getString("demUri");
            Log.d(TAG, "loaded demUri: " + storedDEMUriString);
            if (storedDEMUriString != null) {
                demUri = Uri.parse(storedDEMUriString);
                demSelected(demUri);
            }
            isTargetCoordDisplayed = savedInstanceState.getBoolean("isTargetCoordDisplayed");
        }

        restorePrefOutputMode(); // restore the outputMode from persistent settings
    }

    public int[] getImageDimensionsFromUri(Uri imageUri) {
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(is);
            int width = exif.getAttributeInt( ExifInterface.TAG_IMAGE_WIDTH, -1);
            int height = exif.getAttributeInt( ExifInterface.TAG_IMAGE_LENGTH, -1);
            if (width < 0 || height < 0) {
                return null;
            } else {
                cx = width / 2; // x coordinate of the principal point (center) of the image. Measured from Top-Left corner
                cy = height / 2; // y coordinate of the principal point (center) of the image. Measured from Top-Left corner
                return new int[] {width, height};
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to obtain image dimensions from EXIF metadata!");
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        Log.d(TAG,"onSaveInstanceState started");
        super.onSaveInstanceState(saveInstanceState);
        if (textView != null) {
            saveInstanceState.putCharSequence("textview", textView.getText());
        }
        if (textViewTargetCoord != null) {
            saveInstanceState.putCharSequence("textViewTargetCoord", textViewTargetCoord.getText());
        }
        saveInstanceState.putBoolean("isTargetCoordDisplayed", isTargetCoordDisplayed);
        if (imageUri != null) {
            saveInstanceState.putString("imageUri", imageUri.toString());
        }
        if (demUri != null) {
            Log.d(TAG, "saved demUri: " + demUri.toString());
            saveInstanceState.putString("demUri", demUri.toString());
        }
    }

    public void setButtonReady(Button aButton, boolean isItReady) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float enabled = 1.0f;
                float disabled = 0.5f;

                if (isItReady) {
                    aButton.setAlpha(enabled);
                    aButton.setClickable(true);
                } else {
                    aButton.setAlpha(disabled);
                    aButton.setClickable(false);
                }
            }
        });
    }

    // back from image selection dialog; handle it
    private void imageSelected(Uri uri)
    {
        // save uri for later calculation
        if (imageUri != null && !uri.equals(imageUri)) {
            clearText(); // clear attributes textView
            isTargetCoordDisplayed = false;
            restorePrefOutputMode(); // reset textViewTargetCoord to mode descriptor

            isImageLoaded = false;
            iView.theMarker = null;
            selection_x = -1;
            selection_y = -1;
            cx = -1;
            cy = -1;
        }
        imageUri = uri;

        //appendText("imageSelected: uri is "+uri+"\n");
        //appendText(uri.toString()+"\n");

        //Log.d(TAG,"imageSelected: uri is "+uri);
        //aPath = getPathFromURI(uri);
        //Log.d(TAG,"imageSelected: path is "+aPath);

        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
        } catch(FileNotFoundException e) {
            imageUri = null;
            return;
        }

        long filesize = fileDescriptor.getLength();
        Log.d(TAG, "filesize: " + filesize);
        if (filesize < 1024 * 1024 * 20) { // check if filesize below 20Mb
            iView.setImageURI(uri);
        }  else { // otherwise:
            Toast.makeText(MainActivity.this, getString(R.string.image_is_too_large_error_msg), Toast.LENGTH_SHORT).show();
            iView.setImageResource(R.drawable.athena); // put up placeholder icon
        }

//        appendLog("Selected image "+imageUri+"\n");
        appendText(getString(R.string.image_selected_msg) + "\n");

        getImageDimensionsFromUri(imageUri); // updates cx and cy to that of new image
        isImageLoaded = true;
        if (isDEMLoaded) {
            setButtonReady(buttonCalculate, true);
        }
    }

    private void demSelected(Uri uri) {
//        appendLog("Selected DEM " + uri + "\n");

        //    isDEMLoaded = false;
        setButtonReady(buttonSelectDEM, false);
        setButtonReady(buttonCalculate, false);


        progressBar.setVisibility(View.VISIBLE);

        Handler myHandler = new Handler();

        // Load GeoTIFF in a new thread, this is a long-running task
        new Thread(() -> {
            Exception e = loadDEMnewThread(uri);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (e == null) {
                        String successOutput = "GeoTIFF DEM ";
//            successOutput += "\"" + uri.getLastPathSegment(); + "\" ";
                        successOutput += getString(R.string.dem_loaded_size_is_msg) + " " + theParser.getNumCols() + "x" + theParser.getNumRows() + "\n";
                        appendText(successOutput);
                        printGeoTIFFBounds();
                        isDEMLoaded = true;
                        setButtonReady(buttonSelectImage, true);
                        if (isImageLoaded) {
                            setButtonReady(buttonCalculate, true);
                        }
                        progressBar.setVisibility(View.GONE);
                    } else {
                        appendText(e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }).start();
    }

    private Exception loadDEMnewThread(Uri uri) {
        File appCacheDir = new File(getCacheDir(), "geotiff");
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }
        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        File fileInCache = new File(appCacheDir, uri.getLastPathSegment());
        if (!isCacheUri(uri)) {
            try {
                try (InputStream inputStream = getContentResolver().openInputStream(uri);
                     OutputStream outputStream = new FileOutputStream(fileInCache)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (FileNotFoundException e) {
                    // Handle the FileNotFoundException here
                    // For example, you can show an error message to the user
                    // or log the error to Crashlytics
                    Log.e(TAG, "FileNotFound demSelected()");
                    throw e;
                } catch (IOException e) {
                    // Handle other IOException here
                    // For example, you can log the error to Crashlytics
                    e.printStackTrace();
                    throw e;
                } finally {
                    setButtonReady(buttonSelectDEM, true);
                }
            } catch (Exception e) {
                return e;
            }
        }
        demUri = Uri.fromFile(fileInCache);

        try {
            GeoTIFFParser parser = new GeoTIFFParser(fileInCache);
            theParser = parser;
            theTGetter = new TargetGetter(parser);
            return null;
        } catch (IllegalArgumentException e) {
            String failureOutput = getString(R.string.dem_load_error_generic_msg);
            e.printStackTrace();
            return new Exception(failureOutput + "\n");
        } catch (TiffException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, R.string.wrong_filetype_toast_error_msg, Toast.LENGTH_LONG).show();
                }
            });
            String failureOutput = getString(R.string.dem_load_error_tiffexception_msg);
            e.printStackTrace();
            return new Exception(failureOutput + "\n");
        } finally {
            setButtonReady(buttonSelectDEM, true);
        }
    }

    private boolean isCacheUri(Uri uri) {
        File cacheDir = getCacheDir();
        String cachePath = cacheDir.getAbsolutePath();
        String uriPath = uri.getPath();
        return uriPath.startsWith(cachePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
        if (!isTargetCoordDisplayed) {
            restorePrefOutputMode(); // reset the textViewTargetCoord display
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, getString(R.string.permissions_toast_success_msg), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        super.onDestroy();
    }

    // overloaded, called by button press
    public void calculateImage(View view) {
        calculateImage(view, true);
    }

    public void calculateImage(View view, boolean shouldISendCoT)
    {
        Drawable aDrawable;
        ExifInterface exif;
        String attribs = "Exif information ---\n";

        clearText();
        textViewTargetCoord.setText("");

        appendText(getString(R.string.calculating_target_msg));
//        appendLog("Going to start calculation\n");

        if (imageUri == null) {
//            appendLog("ERROR: Cannot calculate \uD83D\uDEAB\uD83E\uDDEE; no image \uD83D\uDEAB\uD83D\uDDBC selected\n");
            appendText(getString(R.string.no_image_selected_error_msg));
            return;
        }

        // load image into object
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            aDrawable = iView.getDrawable();
            exif = new ExifInterface(is);

            double[] values = theMeta.getMetadataValues(exif);
            double y = values[0];
            double x  = values[1];
            double z = values[2];
            double azimuth = values[3];
            double theta = values[4];

            Log.i(TAG, "parsed xmpMeta\n");

            appendText(getString(R.string.opened_exif_for_image_msg));
            attribs += theMeta.getTagString(ExifInterface.TAG_DATETIME, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_MAKE, exif);
            String make = exif.getAttribute(ExifInterface.TAG_MAKE).toUpperCase();
            attribs += theMeta.getTagString(ExifInterface.TAG_MODEL, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_FOCAL_LENGTH, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
            attribs += theMeta.getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
            double[] intrinsics = theMeta.getIntrinsicMatrixFromExif(exif);
            attribs += getString(R.string.focal_length_label) + Math.round(intrinsics[0]) + "\n";
//            attribs += "fy: " + intrinsics[4] + "\n";
//            attribs += "cx: " + intrinsics[2] + "\n";
//            attribs += "cy: " + intrinsics[5] + "\n";

            double[] relativeRay;
            relativeRay = new double[] {0.0d, 0.0d};
            try {
                if (selection_x < 0 || selection_y < 0 || cx <= 0 || cy <= 0) {
                    throw new NoSuchFieldException("no point was selected");
                } else {
                    relativeRay = theMeta.getRayAnglesFromImgPixel(selection_x, selection_y, exif);

                }
            } catch (Exception e) {
                relativeRay = new double[] {0.0d, 0.0d};
                iView.theMarker = null;
                iView.invalidate();
                Log.i(TAG, "No point was selected in image. Using principal point (center) for calculation.");
            }

            double azimuthOffset = relativeRay[0];
            double thetaOffset = relativeRay[1];

            attribs += getString(R.string.azimuth_offset_label) + " " + Math.round(azimuthOffset) + "°\n";
            attribs += getString(R.string.pitch_offset_label) + " " + -1 * Math.round(thetaOffset) + "°\n";

            DeclinationProvider d = new GeomagneticFieldAdapter();
            double magOffset = (double) d.getMagDeclinationFromLatLonAlt((float) y, (float) x, (float) z);

            // I'm currently assuming DJI and Autel use magnetic north, while Skydio uses true north. Parrot is unknown
            // could be wrong, but seems accurate from testing
            if (make != null && (make.equals("DJI") || make.equals("AUTEL ROBOTICS"))) {
                attribs += "Azimuth offset due to magnetic declination:" + " " + roundDouble(magOffset) + "°\n";
                azimuthOffset -= magOffset;
            }

            azimuth += azimuthOffset;
            theta += thetaOffset;

            if (!outputModeIsSlavic()) {
                attribs += getString(R.string.latitude_label_long) + " "+ roundDouble(y) + "°\n";
                attribs += getString(R.string.longitude_label_long) + " " + roundDouble(x) + "°\n";
                attribs += getString(R.string.altitude_label_long) + " " + Math.round(z) + "m\n";
            } else {
                attribs += getString(R.string.latitude_wgs84_label_long) + " " + roundDouble(y) + "°\n";
                attribs += getString(R.string.longitude_wgs84_label_long) + " " + roundDouble(x) + "°\n";
                attribs += getString(R.string.altiude_wgs84_label_long) + " " + Math.round(z) + "m\n";
            }

            attribs += getString(R.string.attribute_text_drone_azimuth) + " " + Math.round(azimuth) + "°\n";
            attribs += getString(R.string.attribute_text_drone_camera_pitch) + " " + -1 * Math.round(theta) + "°\n";
            appendText(attribs);
            attribs = "";
            double[] result;
            double distance;
            double latitude;
            double longitude;
            double altitudeDouble;
            long altitude;

            double latCK42;
            double lonCK42;
            long altCK42;

            long GK_zone;
            long GK_northing;
            long GK_easting;
            if (theTGetter != null) {
                try {
                    result = theTGetter.resolveTarget(y, x, z, azimuth, theta);
                    distance = result[0];
                    latitude = result[1];
                    longitude = result[2];
                    altitudeDouble = result[3];
                    latCK42 = CoordTranslator.toCK42Lat(latitude, longitude, altitudeDouble);
                    lonCK42 = CoordTranslator.toCK42Lon(latitude, longitude, altitudeDouble);
                    // Note: This altitude calculation assumes the SK42 and WGS84 ellipsoid have the exact same center
                    //     This is not totally correct, but in practice is close enough to the actual value
                    //     @TODO Could be refined at a later time with better math
                    //     See: https://gis.stackexchange.com/a/88499
                    altCK42 = Math.round(altitudeDouble - CoordTranslator.fromCK42Alt(latCK42, lonCK42, 0.0d));

                    long[] GK_conversion_results = CoordTranslator.fromCK42toCK42_GK(latCK42, lonCK42);
                    GK_northing = GK_conversion_results[0];
                    GK_easting = GK_conversion_results[1];

                    altitude = Math.round(result[3]);
                    if (!outputModeIsSlavic()) {
                        attribs += getString(R.string.target_found_at_msg) + ": " + roundDouble(latitude) + "," + roundDouble(longitude) + " Alt: " + altitude + "m" + "\n";
                    } else {
                        attribs += getString(R.string.target_found_at_msg) + " (WGS84): " + roundDouble(latitude) + "," + roundDouble(longitude) + " Alt: " + altitude + "m" + "\n";
                        attribs += getString(R.string.target_found_at_msg) + " (CK-42): " + roundDouble(latCK42) + "," + roundDouble(lonCK42) + " Alt: " + altCK42 + "m" + "\n";
                    }
                    attribs += getString(R.string.drone_dist_to_target_msg) + " " + Math.round(distance) + "m\n";
                    if (!outputModeIsSlavic()) { // to avoid confusion with WGS84, no Google Maps link is provided when outputModeIsSlavic()
                        attribs += "<a href=\"https://maps.google.com/?q=" + roundDouble(latitude) + "," + roundDouble(longitude) + "\">";
                        attribs += "maps.google.com/?q=" + roundDouble(latitude) + "," + roundDouble(longitude) + "</a>\n\n";
                    }
                } catch (RequestedValueOOBException e) {
                    if (e.isAltitudeDataBad) {
                        Log.e(TAG, e.getMessage());
                        attribs += getString(R.string.bad_altitude_data_error_msg) + "\n";
                        appendText(attribs);
                        return;
                    } else {
                        Log.e(TAG, "ERROR: resolveTarget ran OOB at (WGS84): " + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon));
                        if (!outputModeIsSlavic()) {
                            attribs += getString(R.string.resolveTarget_oob_error_msg) + ":" + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon) + "\n";
                        } else {
                            attribs += getString(R.string.resolveTarget_oob_error_msg) + " (CK-42):" + roundDouble(CoordTranslator.toCK42Lat(e.OOBLat, e.OOBLon, z)) + ", " + roundDouble(CoordTranslator.toCK42Lon(e.OOBLat, e.OOBLon, z)) + "\n";
                        }
                        attribs += getString(R.string.geotiff_coverage_reminder);
                        attribs += getString(R.string.geotiff_coverage_precedent_message);
                        appendText(attribs);
                        printGeoTIFFBounds();
                        return;
                    }
                }
            } else {
                attribs += getString(R.string.geotiff_load_reminder_msg);
                appendText(attribs);
                return;
            }
            attribs = attribs.replaceAll("(\r\n|\n)", "<br>"); // replace newline with HTML equivalent
            textView.append(Html.fromHtml(attribs, 0, null, null));
            // Obtain NATO MGRS from mil.nga.mgrs library
            MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
            String mgrs1m = mgrsObj.coordinate(GridType.METER);
            String mgrs10m = mgrsObj.coordinate(GridType.TEN_METER);
            String mgrs100m = mgrsObj.coordinate(GridType.HUNDRED_METER);
            String targetCoordString;
            if (!outputModeIsSlavic()) {
                targetCoordString = "<a href=\"https://maps.google.com/?q=";
                if (outputModeIsMGRS()) {
                    targetCoordString += mgrs1m; // use MGRS 1m for maps link, even if on 10m or 100m mode
                } else {
                    targetCoordString += roundDouble(latitude) + "," + roundDouble(longitude); // otherwise just use normal WGS84
                }
                targetCoordString += "\">"; // close start of href tag
                if (outputModeIsMGRS()) {
                    switch(outputMode) {
                        case MGRS1m:
                            targetCoordString += mgrs1m;
                            break;
                        case MGRS10m:
                            targetCoordString += mgrs10m;
                            break;
                        case MGRS100m:
                            targetCoordString += mgrs100m;
                            break;
                        default:
                            throw new RuntimeException("Program entered an inoperable state due to outputMode"); // this shouldn't ever happen
                    }
                } else {
                    targetCoordString += roundDouble(latitude) + ", " + roundDouble(longitude);
                }
                targetCoordString += "</a> "; // end href tag
                targetCoordString += getString(R.string.altitude_label_short) + " " + altitude + "m";
            } else { // to avoid confusion with WGS84, no Google Maps link is provided when outputModeIsSlavic()
                if (outputMode == outputModes.CK42Geodetic) {
                    targetCoordString = "(CK-42) " + roundDouble(latCK42) + ", " + roundDouble(lonCK42) + " Alt: " + altCK42 + "m" + "<br>";
                } else if (outputMode == outputModes.CK42GaussKrüger) {
                    String northing_string = makeGKHumanReadable(GK_northing);
                    String easting_string = makeGKHumanReadable(GK_easting);
                    targetCoordString = "(CK-42) [Gauss-Krüger] " + "<br>" + getString(R.string.gk_northing_text) + " " + northing_string + "<br>" + getString(R.string.gk_easting_text) + " " + easting_string + "<br>" + getString(R.string.altitude_label_short) + " " + altCK42 + "m\n";
                } else {
                    throw new RuntimeException("Program entered an inoperable state due to outputMode"); // this shouldn't ever happen
                }
            }
            textViewTargetCoord.setText(Html.fromHtml(targetCoordString, 0, null, null));
            isTargetCoordDisplayed = true;
            // close file
            is.close();
            //
            // send CoT message to udp://239.2.3.1:6969
            //     e.g. for use with DoD's ATAK app
            if (shouldISendCoT) {
                CursorOnTargetSender.sendCoT(this, latitude, longitude, altitudeDouble, theta, exif.getAttribute(ExifInterface.TAG_DATETIME));
            }
        } catch (XMPException e) {
            Log.e(TAG, e.getMessage());
            appendText(getString(R.string.metadata_parse_error_msg) + e + "\n");
            e.printStackTrace();
        } catch (MissingDataException e) {
            Log.e(TAG, e.getMessage());
            appendText(e.getMessage() + "\n");
            e.getStackTrace();
        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
            appendText(getString(R.string.metadata_parse_error_msg)+e+"\n\n");
            e.printStackTrace();
        }
    } // button click

    private String makeGKHumanReadable(long GK) {
        String human_readable;
        if (GK >= 10000000) {
            human_readable = Long.toString(GK);
        } else { // If value is not at least 5 digits, pad with leading zeros
            human_readable = Long.toString(GK + 10000000);
            human_readable = human_readable.substring(1);
        }
        human_readable = human_readable.substring(0, human_readable.length() - 5) + "-" + human_readable.substring(human_readable.length() - 5);
        return human_readable;
    }

    private void printGeoTIFFBounds() {
        String attribs = "";
        if (!outputModeIsSlavic()) {
            attribs += roundDouble(theParser.getMinLat()) + " ≤ " + getString(R.string.latitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
            attribs += roundDouble(theParser.getMinLon()) + " ≤ " + getString(R.string.longitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLon()) + "\n\n";
        } else {
            try {
                // Believe me, I don't like this either....
                attribs += roundDouble(CoordTranslator.toCK42Lat(theParser.getMinLat(), theParser.getMinLon(), theParser.getAltFromLatLon(theParser.getMinLat(), theParser.getMinLon()))) + " ≤ " + getString(R.string.latitude_label_short) + " " + "(CK-42)" + " ≤ " + roundDouble(CoordTranslator.toCK42Lat(theParser.getMaxLat(), theParser.getMaxLon(), theParser.getAltFromLatLon(theParser.getMaxLat(), theParser.getMaxLon()))) + "\n";
                attribs += roundDouble(CoordTranslator.toCK42Lon(theParser.getMinLat(), theParser.getMinLon(), theParser.getAltFromLatLon(theParser.getMinLat(), theParser.getMinLon()))) + " ≤ " + getString(R.string.longitude_label_short) + " " + "(CK-42)" + " ≤ " + roundDouble(CoordTranslator.toCK42Lon(theParser.getMaxLat(), theParser.getMaxLon(), theParser.getAltFromLatLon(theParser.getMaxLat(), theParser.getMaxLon()))) + "\n\n";
            } catch (RequestedValueOOBException e_OOB) { // This shouldn't happen, may be possible though if GeoTIFF file is very small
                // revert to WGS84 if CK-42 conversion has failed
                attribs += getString(R.string.wgs84_ck42_conversion_fail_warning);
                attribs += roundDouble(theParser.getMinLat()) + " ≤ " + getString(R.string.latitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
                attribs += roundDouble(theParser.getMinLon()) + " ≤ " + getString(R.string.longitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLon()) + "\n\n";
            }
        }
        appendText(attribs);
    }

    public void displayAutelAlert() {
        if (dangerousAutelAwarenessCount < 3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.autel_accuracy_warning_msg);
            builder.setPositiveButton(R.string.i_understand_this_risk, (DialogInterface.OnClickListener) (dialog, which) -> {
                dangerousAutelAwarenessCount += 1;
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void copyTargetCoordText(View view) {
        if (isTargetCoordDisplayed) {
            String text = textViewTargetCoord.getText().toString();
            text = text.replaceAll("<[^>]*>", ""); // remove HTML link tag(s)

            // Copy the text to the clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Text", text);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, getString(R.string.text_copied_to_clipboard_msg), Toast.LENGTH_SHORT).show();
        }
    }

    // select image button clicked; launch chooser and get result
    // in callback
    public void selectImage(View view)
    {
        Log.d(TAG,"selectImageClick started");
        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));

        requestExternStorage();

//        appendLog("Going to start selecting image\n");
        //appendText("selectImageClick started\n");

        //Intent i = new Intent();
        //i.setType("image/*");
        //i.setAction(Intent.ACTION_GET_CONTENT);
        //startActivity(Intent.createChooser(i,"Select Picture"));

        mGetContent.launch("image/*");

        // pass the constant to compare it
        // with the returned requestCode
        // StartActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);

        //appendText("Chooser started\n");
//        appendLog("Chooser started\n");
    }

    public void selectDEM(View view)
    {
        Log.d(TAG,"selectDEM started");
//        appendLog("Going to start selecting GeoTIFF\n");

        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));

        requestExternStorage();

        mGetDEM.launch("image/*");

    }

    private void requestExternStorage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(TAG, "Attempting to Obtain unobtained storage permissions");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, requestNo);
                requestNo++;
            }
        }
    }

    private String roundDouble(double d) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.######", decimalSymbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(d);
    }

    private void appendText(final String aStr)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(aStr);

            }
        });

    } // appendText to textView but do so on UI thread

    // reset the text field
    private void clearText()
    {
        runOnUiThread(new Runnable() {
           @Override
           public void run() {
               String placeholderText = getString(R.string.openathena_for_android) +  " " + getString(R.string.version_word) + " " + versionName + "\n\n";
               placeholderText += getString(R.string.step_1_load_a_DEM) + " \u26F0\n";
               placeholderText += getString(R.string.step_2_load_drone_image) + " \uD83D\uDDBC\n";
               placeholderText += getString(R.string.step_3_press_calculate) + " \uD83E\uDDEE\n";
               placeholderText += getString(R.string.step_4_obtain_target) + " \uD83C\uDFAF\n\n";
               textView.setText(placeholderText);
           }
        });

    }

    private void appendLog(String str)
    {
        FileOutputStream fos;
        PrintWriter pw;

        Log.d(TAG,"appendLog started");

        try {
            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE|Context.MODE_APPEND);
            pw = new PrintWriter(fos);
            pw.print(str);
            pw.close();
            fos.close();
            Log.d(TAG,"appendLog: wrote to logfile");

        } catch (Exception e) {
            Log.d(TAG,"appendLog: failed to write log:"+e.getMessage());
        }

    } // appendLog()

}
