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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    public final static String PREFS_NAME = "openathena.preferences";
    public static String TAG = MainActivity.class.getSimpleName();
    public final static String LOG_NAME = "openathena.log";
    public static int requestNo = 0;

    TextView textView;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    protected String versionName;
    ImageView iView;
    Uri imageUri = null;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        //appendText("Back from chooser\n");

                        if (uri == null)
                            return;

                        //appendText("Back from chooser\n");
                        Log.d(TAG,"back from chooser");
                        imageSelected(uri);
                    }
                });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.test_layout);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.textView);
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

    // stolen from https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    // modified by rdk
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
            appendText("getFileName: using cursor thingys\n");
        }
        if (result == null) {
            appendText("getFileName: using uri path\n");
            result = uri.getPath();

            //int cut = result.lastIndexOf('/');
            //if (cut != -1) {
              //  result = result.substring(cut + 1);
            //}
        }
        return result;
    }

    // back from image selection dialog; handle it
    private void imageSelected(Uri uri)
    {
        String aPath;

        // save uri for later calculation
        imageUri = uri;

        //appendText("imageSelected: uri is "+uri+"\n");
        //appendText(uri.toString()+"\n");

        //Log.d(TAG,"imageSelected: uri is "+uri);
        //aPath = getPathFromURI(uri);
        //Log.d(TAG,"imageSelected: path is "+aPath);

/*
        iView.setImageURI(uri);
*/
        appendLog("Selected image "+imageUri+"\n");
        // Android 10/11, we can't access this file directly
        // @TODO will need to copy file into app's own package cache
        GeoTIFFParser p = new GeoTIFFParser(new File(uri.getPath()));

        //appendText("Image selected "+aPath+"\n");
        //appendLog("Image selected "+aPath+"\n");
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
        Log.d(TAG, permissions[0]);
        Log.d(TAG, Integer.toString(grantResults[0]));
        Log.d(TAG, Integer.toString(PackageManager.PERMISSION_GRANTED));
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
        File aFile;
        String attribs = "Exif information ---\n";

        clearText();

        //appendText("calculateImage clicked\n");
        appendLog("Going to start calculation\n");

        if (imageUri == null) {
            appendLog("Cannot calculate; no image selected\n");
            appendText("Cannot calculate: no image selected\n");
            return;
        }

        // load image into object
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            aDrawable = iView.getDrawable();
            exif = new ExifInterface(is);
            appendText("Opened exif for image\n");
            attribs += getTagString(ExifInterface.TAG_DATETIME, exif);
            attribs += getTagString(ExifInterface.TAG_MAKE, exif);
            attribs += getTagString(ExifInterface.TAG_MODEL, exif);
            Float[] yxz = exifGetYXZ(exif);
            float y = yxz[0];
            float x = yxz[1];
            float z = yxz[2];
            attribs += "Latitude : " + y + "\n";
            attribs += "Longitude : " + x + "\n";
            attribs += "Altitude : " + z + "\n";
            attribs += getTagString(ExifInterface.TAG_ORIENTATION, exif);
            appendText(attribs+"\n");

            // close file
            is.close();
            //
        } catch (Exception e) {
            appendText("Unable to open image file to calculate: "+e+"\n");
        }


    } // button click

    // select image button clicked; launch chooser and get result
    // in callback
    public void selectImage(View view)
    {
        Log.d(TAG,"selectImageClick started");
        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            Log.d(TAG,"Attempting to Obtain unobtained storage permissions");
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE } , requestNo);
            requestNo++;
            // @TODO should actually end call here once Scoped Storage works properly
            // return
        }

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

    public void loadDEM(View view)
    {
        Log.d(TAG,"loadDEM started");
        appendLog("Going to start selecting GeoTIFF\n");
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
        y += Float.valueOf(rationalToFloat(latArr[0]));
        y += Float.valueOf(rationalToFloat(latArr[1])) / 60.0f;
        y += Float.valueOf(rationalToFloat(latArr[2])) / 3600.0f;
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
        float numerator = Float.valueOf(split[0]);
        float denominator = Float.valueOf(split[1]);
        return numerator / denominator;
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