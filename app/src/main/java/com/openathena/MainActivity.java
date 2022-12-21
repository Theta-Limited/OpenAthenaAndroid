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
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Html;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
import mil.nga.mgrs.grid.GridType;
import mil.nga.tiff.util.TiffException;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;


public class MainActivity extends AppCompatActivity {

    public final static String PREFS_NAME = "openathena.preferences";
    public static String TAG = MainActivity.class.getSimpleName();
    public final static String LOG_NAME = "openathena.log";
    public static int requestNo = 0;

    TextView textView;
    TextView textViewMGRS;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    protected String versionName;
    ImageView iView;
    Uri imageUri = null;
    Uri demUri = null;
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

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textViewMGRS = (TextView)findViewById(R.id.textViewMGRS);
        textViewMGRS.setMovementMethod(LinkMovementMethod.getInstance());

        iView = (ImageView)findViewById(R.id.imageView);

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

        textView.setText("OpenAthena for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\n GPL-3.0, some rights reserved\n");
        appendLog("OpenAthena for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\n GPL-3.0, some rights reserved\n");

    }

    // stolen from InterWebs
    // https://mobikul.com/pick-image-gallery-android/
    private String getPathFromURI(Uri uri)
    {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

//    // stolen from https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
//    // modified by rdk
//    @SuppressLint("Range")
//    private String getFileName(Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            } finally {
//                cursor.close();
//            }
//            appendText("getFileName: using cursor thingys\n");
//        }
//        if (result == null) {
//            appendText("getFileName: using uri path\n");
//            result = uri.getPath();
//
//            //int cut = result.lastIndexOf('/');
//            //if (cut != -1) {
//              //  result = result.substring(cut + 1);
//            //}
//        }
//        return result;
//    }

    // back from image selection dialog; handle it
    private void imageSelected(Uri uri)
    {
        // save uri for later calculation
        if (imageUri != null && !uri.equals(imageUri)) {
            clearText();
            textViewMGRS.setText(R.string.nato_mgrs);
        }
        imageUri = uri;

        //appendText("imageSelected: uri is "+uri+"\n");
        //appendText(uri.toString()+"\n");

        //Log.d(TAG,"imageSelected: uri is "+uri);
        //aPath = getPathFromURI(uri);
        //Log.d(TAG,"imageSelected: path is "+aPath);

        iView.setImageURI(uri);


        appendLog("Selected image "+imageUri+"\n");
        appendText(getString(R.string.image_selected_msg));
    }

    private void demSelected(Uri uri) {
        appendLog("Selected DEM " + uri + "\n");
        File appCacheDir = new File(getCacheDir(), "geotiff");
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }
        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        File fileInCache = new File(appCacheDir, uri.getLastPathSegment());
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
            return;
        } catch (IOException e) {
            // Handle other IOException here
            // For example, you can log the error to Crashlytics
            e.printStackTrace();
            return;
        }
        demUri = Uri.fromFile(fileInCache);

        Toast.makeText(MainActivity.this, "Loading GeoTIFF. Please wait...", Toast.LENGTH_SHORT).show();

        try {
            GeoTIFFParser parser = new GeoTIFFParser(fileInCache);
            theParser = parser;
            theTGetter = new TargetGetter(parser);
            String successOutput = "GeoTIFF DEM ";
//            successOutput += "\"" + uri.getLastPathSegment(); + "\" ";
            successOutput += getString(R.string.dem_loaded_size_is_msg) + theParser.getNumCols() + "x" + theParser.getNumRows() + "\n";
            successOutput += roundDouble(theParser.getMinLat()) + " ≤ lat ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
            successOutput += roundDouble(theParser.getMinLon()) + " ≤ lon ≤ " + roundDouble(theParser.getMaxLon()) + "\n";
            appendText(successOutput);
        } catch (IllegalArgumentException e) {
            String failureOutput = getString(R.string.dem_load_error_generic_msg) + e.getMessage() + "\n";
            appendText(failureOutput);
        } catch (TiffException e) {
            String failureOutput = getString(R.string.dem_load_error_tiffexception_msg);
            appendText(failureOutput);
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

        // don't do anything if user chooses Calculate; we're already there.

        if (id == R.id.action_prefs) {
            intent = new Intent(getApplicationContext(), PrefsActivity.class);
            // https://stackoverflow.com/questions/8688099/android-switch-to-activity-without-restarting-it
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

        if (id == R.id.action_log) {
            intent = new Intent(getApplicationContext(),ActivityLog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Failed to Obtain Necessary Permissions", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");
        //appendText("onPause\n");
        super.onPause();

    } // onPause()

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        //appendText("onDestroy\n");
        // do whatever here

        // no need to close logfile; its closed after each use

        super.onDestroy();

    } // onDestroy()

    // http://android-er.blogspot.com/2009/12/read-exif-information-in-jpeg-file.html
    private String getTagString(String tag, ExifInterface exif)
    {
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }

    public void calculateImage(View view)
    {
        Drawable aDrawable;
        ExifInterface exif;
        String xmp_str;
        XMPMeta xmpMeta;
        String attribs = "Exif information ---\n";

        clearText();
        textViewMGRS.setText("");

        appendText(getString(R.string.calculating_target_msg));
        appendLog("Going to start calculation\n");

        if (imageUri == null) {
            appendLog("ERROR: Cannot calculate \uD83D\uDEAB\uD83E\uDDEE; no image \uD83D\uDEAB\uD83D\uDDBC selected\n");
            appendText(getString(R.string.no_image_selected_error_msg));
            return;
        }

        // load image into object
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            aDrawable = iView.getDrawable();
            exif = new ExifInterface(is);
            appendText(getString(R.string.opened_exif_for_image_msg));
            xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
            Log.i(TAG, "XMPString: " + xmp_str);
            xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());
            Log.i(TAG, "parsed xmpMeta\n");
            attribs += getTagString(ExifInterface.TAG_DATETIME, exif);
            attribs += getTagString(ExifInterface.TAG_MAKE, exif);
            attribs += getTagString(ExifInterface.TAG_MODEL, exif);
//            // get lat lon alt from EXIF
//            Float[] yxz = exifGetYXZ(exif);
//            float y = yxz[0];
//            float x = yxz[1];
//            float z = yxz[2];
            // get lat lon alt from XMP within EXIF
            double y = Double.parseDouble(xmpMeta.getPropertyString("http://www.dji.com/drone-dji/1.0/", "GpsLatitude"));
            double x = Double.parseDouble(xmpMeta.getPropertyString("http://www.dji.com/drone-dji/1.0/", "GpsLongitude"));
            double z = Double.parseDouble(xmpMeta.getPropertyString("http://www.dji.com/drone-dji/1.0/", "AbsoluteAltitude"));

            attribs += "Latitude : " + y + "\n";
            attribs += "Longitude : " + x + "\n";
            attribs += "Altitude : " + z + "\n";
            String gimbalYawDegree = xmpMeta.getPropertyString("http://www.dji.com/drone-dji/1.0/", "GimbalYawDegree");
            attribs += "GimbalYawDegree: " + gimbalYawDegree + "\n";
            String gimbalPitchDegree = xmpMeta.getPropertyString("http://www.dji.com/drone-dji/1.0/", "GimbalPitchDegree");
            attribs += "GimbalPitchDegree: " + gimbalPitchDegree + "\n";
            appendText(attribs);
            attribs = "";
            double[] result;
            double distance;
            double latitude;
            double longitude;
            long altitude;
            if (theTGetter != null) {
                try {
                    result = theTGetter.resolveTarget(y, x, z, Double.parseDouble(gimbalYawDegree), Double.parseDouble(gimbalPitchDegree));
                    distance = result[0];
                    latitude = result[1];
                    longitude = result[2];
                    altitude = Math.round(result[3]);
                    attribs += getString(R.string.target_found_at_msg) + roundDouble(latitude) + "," + roundDouble(longitude) + " Alt: " + altitude + "m" + "\n";
                    attribs += getString(R.string.drone_dist_to_target_msg) + Math.round(distance) + "m\n";
                    attribs += "<a href=\"https://maps.google.com/?q=" + roundDouble(latitude) + "," + roundDouble(longitude) + "\">";
                    attribs += "maps.google.com/?q=" + roundDouble(latitude) + "," + roundDouble(longitude) + "</a>\n";
                } catch (RequestedValueOOBException e) {
                    if (e.isAltitudeDataBad) {
                        Log.e(TAG, e.getMessage());
                        attribs += getString(R.string.bad_altitude_data_error_msg) + "\n";
                        appendText(attribs);
                        return;
                    } else {
                        Log.e(TAG, "ERROR: resolveTarget ran OOB at: " + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon));
                        attribs += getString(R.string.resolveTarget_oob_error_msg) + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon) + "\n";
                        attribs += getString(R.string.geotiff_coverage_reminder);
                        attribs += getString(R.string.geotiff_coverage_precedent_message);
                        attribs += roundDouble(theParser.getMinLat()) + " ≤ lat ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
                        attribs += roundDouble(theParser.getMinLon()) + " ≤ lon ≤ " + roundDouble(theParser.getMaxLon()) + "\n";
                        appendText(attribs);
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
            String mgrsString = "<a href=\"https://maps.google.com/?q=" + mgrs1m + "\">";
            mgrsString += mgrs1m + "</a> ";
            mgrsString += "Alt: " + altitude + "m";
            textViewMGRS.setText(Html.fromHtml(mgrsString, 0, null, null));
            // close file
            is.close();
            //
        } catch (XMPException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            appendText(getString(R.string.metadata_parse_error_msg)+e+"\n");
            e.printStackTrace();
        }
    } // button click

    public void copyMGRSText(View view) {
        String text = textViewMGRS.getText().toString();
        text = text.replaceAll("<[^>]*>", ""); // remove HTML link tag(s)

        // Copy the text to the clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, getString(R.string.text_copied_to_clipboard_msg), Toast.LENGTH_SHORT).show();
    }

    // select image button clicked; launch chooser and get result
    // in callback
    public void selectImage(View view)
    {
        Log.d(TAG,"selectImageClick started");
        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));

        requestExternStorage();

        appendLog("Going to start selecting image\n");
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
        appendLog("Chooser started\n");
    }

    public void selectDEM(View view)
    {
        Log.d(TAG,"selectDEM started");
        appendLog("Going to start selecting GeoTIFF\n");

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

    private Float[] exifGetYXZ(ExifInterface exif)
    {
        String latDir = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        latDir = latDir.toUpperCase();
        String latRaw = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String[] latArr = latRaw.split(",", 3);
        String lonDir = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        lonDir = lonDir.toUpperCase();
        String lonRaw = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String[] lonArr = lonRaw.split(",", 3);
        String alt = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);

        float y = 0.0f;
        y += rationalToFloat(latArr[0]);
        y += rationalToFloat(latArr[1]) / 60.0f;
        y += rationalToFloat(latArr[2]) / 3600.0f;
        if (latDir.equals("S"))
        {
            y = y * -1.0f;
        }

        float x = 0.0f;
        x += rationalToFloat(lonArr[0]);
        x += rationalToFloat(lonArr[1]) / 60.0f;
        x += rationalToFloat(lonArr[2]) / 3600.0f;
        if (lonDir.equals("W"))
        {
            x = x * -1.0f;
        }

        float z = rationalToFloat(alt);

        Float[] arrOut = {y, x, z};
        return(arrOut);
    }

    private float rationalToFloat(String str)
    {
        String[] split = str.split("/", 2);
        float numerator = Float.parseFloat(split[0]);
        float denominator = Float.parseFloat(split[1]);
        return numerator / denominator;
    }

    private String roundDouble(double d) {
        DecimalFormat df = new DecimalFormat("#.######");
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
               textView.setText("OpenAthena for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\nGPL-3.0, some rights reserved\n");
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
